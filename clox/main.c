#include "common.h"
#include "chunk.h"
#include "debug.h"
#include "vm.h"
#include <stdio.h>

int main(int argc, const char* argv[]) {

    initVM();

    Chunk chunk;
    initChunk(&chunk);
    
    writeConstant(&chunk, 1, 123);
    writeConstant(&chunk, 2, 123);
    writeConstant(&chunk, 3, 123);
    writeChunk(&chunk, OP_MULTIPLY, 123);
    writeChunk(&chunk, OP_ADD, 123);
    writeConstant(&chunk, 4, 123);
    writeConstant(&chunk, 5, 123);
    writeChunk(&chunk, OP_NEGATE, 123);
    writeChunk(&chunk, OP_DIVIDE, 123);
    writeChunk(&chunk, OP_SUBTRACT, 123);
    writeChunk(&chunk, OP_RETURN, 123);
    
    disassembleChunk(&chunk, "test chunk");

    interpret(&chunk);

    freeVM();
    freeChunk(&chunk);
    
    return 0;
}