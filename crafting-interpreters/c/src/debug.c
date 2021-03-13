#include <stdio.h>
#include "debug.h"
#include "value.h"

void disassembleChunk(Chunk* chunk, const char* name) {
    printf("== %s ==\n", name);
    for (int offset = 0; offset < chunk->count;) {
        offset = disassembleInstruction(chunk, offset);
    }
}

static int simpleInstruction(const char* name, int offset) {
    printf("%s\n", name);
    return offset + 1;
}

static int  byteInstruction(const char* name, Chunk* chunk,int offset) {
    uint8_t slot = chunk->code[offset + 1];
    printf("%-16s %4d\n", name, slot);
    return offset + 2; 
}

static uint8_t consumeConstantInstruction(Chunk* chunk, int* offset) {
    uint8_t constant = chunk->code[*offset + 1];
    *offset = *offset + 2;
    return constant;
}

static int constantInstruction(const char* name, Chunk* chunk, int offset) {
    uint8_t constant = consumeConstantInstruction(chunk, &offset);
    printf("%-16s %4d '", name, constant);
    printValue(chunk->constants.values[constant]);
    printf("'\n");
    return offset;
}

static int jumpInstruction(
    const char* name,
    int sign,
    Chunk* chunk,
    int offset
) {
    uint16_t jump = (uint16_t)(chunk->code[offset + 1] << 8);
    jump |= chunk->code[offset + 2];
    printf("%-16s %4d -> %d\n", name, offset, offset + 3 + sign * jump);
    return offset + 3;
}

static uint32_t consumeLongConstantInstruction(Chunk* chunk, int* offset) {
    uint32_t constant = chunk->code[*offset + 1] |
        (chunk->code[*offset + 2] << 8) |
        (chunk->code[*offset + 3] << 16);
    *offset = *offset + 4;
    return constant;
}

static int longConstantInstruction(const char* name, Chunk* chunk, int offset) {
    uint32_t constant = consumeLongConstantInstruction(chunk, &offset);
    printf("%-16s %8d '", name, constant);
    printValue(chunk->constants.values[constant]);
    printf("'\n");
    return offset + 4;
}

int disassembleInstruction(Chunk* chunk, int offset) {
    printf("%04d ", offset);
    int line = getLine(chunk, offset);
    if (offset > 0 && line == getLine(chunk, offset - 1)) {
        printf("   | ");
    } else {
        printf("%4d ", line);
    }
    uint8_t instruction = chunk->code[offset];
    switch (instruction) {
        case OP_CONSTANT:
            return constantInstruction("OP_CONSTANT", chunk, offset);
        case OP_CONSTANT_LONG:
            return longConstantInstruction("OP_CONSTANT_LONG", chunk, offset);
        case OP_NIL: 
            return simpleInstruction("OP_NIL", offset);
        case OP_TRUE:
            return simpleInstruction("OP_TRUE", offset);
        case OP_FALSE:
            return simpleInstruction("OP_FALSE", offset);
        case OP_EQUAL:
            return simpleInstruction("OP_EQUAL", offset);
        case OP_LESS:
            return simpleInstruction("OP_LESS", offset);
        case OP_GREATER:
            return simpleInstruction("OP_GREATER", offset);
        case OP_ADD:
            return simpleInstruction("OP_ADD", offset);    
        case OP_SUBTRACT:
            return simpleInstruction("OP_SUBTRACT", offset);
        case OP_MULTIPLY:
            return simpleInstruction("OP_MULTIPLY", offset);
        case OP_DIVIDE:
            return simpleInstruction("OP_DIVIDE", offset);
        case OP_NEGATE:
            return simpleInstruction("OP_NEGATE", offset);
        case OP_NOT:
            return simpleInstruction("OP_NOT", offset);
        case OP_DEFINE_GLOBAL:
            return simpleInstruction("OP_DEFINE_GLOBAL", offset);
        case OP_GET_GLOBAL:
            return simpleInstruction("OP_GET_GLOBAL", offset);
        case OP_SET_GLOBAL:
            return simpleInstruction("OP_SET_GLOBAL", offset);
        case OP_GET_LOCAL:
            return byteInstruction("OP_GET_LOCAL", chunk, offset);
        case OP_SET_LOCAL:
            return byteInstruction("OP_SET_LOCAL", chunk, offset);
        case OP_CLOSE_UPVALUE:
            return simpleInstruction("OP_CLOSE_UPVALUE", offset);
        case OP_GET_UPVALUE:
            return byteInstruction("OP_GET_UPVALUE", chunk, offset);
        case OP_SET_UPVALUE:
            return byteInstruction("OP_SET_UPVALUE", chunk, offset);
        case OP_POP:
            return simpleInstruction("OP_POP", offset);
        case OP_PRINT:
            return simpleInstruction("OP_PRINT", offset);
        case OP_LOOP:
            return jumpInstruction("OP_LOOP", 0, chunk, offset);
        case OP_JUMP:
            return jumpInstruction("OP_JUMP", 1, chunk, offset);
        case OP_JUMP_IF_FALSE:
            return jumpInstruction("OP_JUMP_IF_FALSE", 1, chunk, offset);
        case OP_CALL:
            return byteInstruction("OP_CALL", chunk, offset);
        case OP_CLOSURE: {
            /*uint32_t constant;
            uint8_t constantInstruction = chunk->code[++offset];
            switch (constantInstruction) {
                case OP_CONSTANT:
                    constant = consumeConstantInstruction(chunk, &offset);
                    break; 
                case OP_CONSTANT_LONG:
                    constant = consumeLongConstantInstruction(chunk, &offset);
                    break;
            }
            printf("%-16s %8d ", "OP_CLOSURE", constant);
            printValue(chunk->constants.values[constant]);
            printf("\n");
            return offset;
            */
            offset = byteInstruction("OP_CLOSURE", chunk, offset);
            uint8_t upvalues = chunk->code[offset - 1];
            for (int i = 0; i  < upvalues; ++i) {
                int isLocal = chunk->code[offset++];
                int index = chunk->code[offset++];
                printf("%04d    |                     %s %d\n", 
                    offset - 2, isLocal ? "local" : "upvalue", index);
            }
            return offset;
            // ObjFunction* function = AS_FUNCTION(chunk->constants.values[constant]);
        }
        case OP_RETURN:
            return simpleInstruction("OP_RETURN", offset);
        default:
            printf("Unknown opcode %d\n", instruction);
            return offset + 1;
    }
}
