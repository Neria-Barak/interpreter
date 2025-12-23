#include <stdlib.h>
#include "chunk.h"
#include "memory.h"
#include "vm.h"

#include <stdio.h>

void initChunk(Chunk* chunk) {
    chunk->capacity = 0;
    chunk->count = 0;
    chunk->lineCount = 0;
    chunk->lineCapacity = 0;
    chunk->code = NULL;
    chunk->lines = NULL;
    initValueArray(&chunk->constants);
}

static void addLine(Chunk* chunk, int line) {
    if (chunk->lineCount > 0 && chunk->lines[chunk->lineCount].line == line) {
        chunk->lines[chunk->lineCount].count++;
        return;
    }

    if (chunk->lineCapacity < chunk->lineCount + 1) {
        int oldCapacity = chunk->lineCapacity;
        chunk->lineCapacity = GROW_CAPACITY(oldCapacity);
        chunk->lines = GROW_ARRAY(LineInfo, chunk->lines, oldCapacity, chunk->lineCapacity);
    }

    chunk->lines[chunk->lineCount].count = 1;
    chunk->lines[chunk->lineCount].line = line;
    chunk->lineCount++;
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    if (chunk->capacity < chunk->count + 1) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(uint8_t, chunk->code, oldCapacity, chunk->capacity);
    }

    chunk->code[chunk->count] = byte;
    chunk->count++;
    addLine(chunk, line);
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(int, chunk->lines, chunk->capacity);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

int addConstant(Chunk* chunk, Value value) {
    push(value);
    writeValueArray(&chunk->constants, value);
    pop();
    return chunk->constants.count - 1;
}

void writeConstant(Chunk* chunk, Value value, int line) {
    int constant = addConstant(chunk, value);
    if (constant > UINT8_MAX) {
        writeChunk(chunk, OP_CONSTANT_LONG, line);
        writeChunk(chunk, (constant >> 16) & 0xFF, line);
        writeChunk(chunk, (constant >> 8 ) & 0xFF, line);
        writeChunk(chunk, (constant      ) & 0xFF, line);
    } else {
        writeChunk(chunk, OP_CONSTANT, line);
        writeChunk(chunk, constant, line);
    }
}

int getLine(Chunk* chunk, int index) {
    int i = 0;
    int instructionCount = chunk->lines[0].count;
    for (i = 1; instructionCount < index; i++) {
        instructionCount += chunk->lines[i].count;
    }
    return chunk->lines[i].line;
}
