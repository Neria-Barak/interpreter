#ifndef COMPILER_H
#define COMPILER_H

#include "chunk.h"
#include "object.h"

bool compile(const char* source, Chunk* chunk);

#endif