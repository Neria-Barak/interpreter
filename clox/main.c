#include "common.h"
#include "chunk.h"
#include "debug.h"
#include <stdio.h>

int main(int argc, const char* argv[]) {
    Chunk chunk;
    initChunk(&chunk);

    // int constant = addConstant(&chunk, 1.2);

    // writeChunk(&chunk, OP_CONSTANT, 123);
    // writeChunk(&chunk, constant, 123);
    writeConstant(&chunk, 1.2, 123);

    writeChunk(&chunk, OP_RETURN, 123);

    for (int i = 0; i < 300; i++) {
        writeConstant(&chunk, i, 130 + i);
    }
    

    disassembleChunk(&chunk, "test chunk");

    // printf("%d\n", getLine(&chunk, 0));
    // printf("%d\n", getLine(&chunk, 1));

    freeChunk(&chunk);
    
    return 0;
}