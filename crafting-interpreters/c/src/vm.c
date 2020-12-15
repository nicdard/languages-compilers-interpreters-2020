#include <stdarg.h>
#include <stdio.h>

#include "common.h"
#include "vm.h"
#include "debug.h"
#include "compiler.h"

/**
 * Runs the current chunk of code.
 */
static InterpretResult run();
/**
 * Empties the VM's stack.
 */
static void resetStack();
/**
 * Access the Value stored in the stack at distance.
 */
static Value peek(int distance);
/**
 * Report a Runtime Error.
 */
static void runtimeError(const char* format, ...);
/**
 * Returns true if the value is either nil or false.
 */
static bool isFalsey(Value value);

/**
 * The global instance of the VM. 
 * It would be better not to use a global variable,
 * but here is done for the sake of simplicity. 
 */ 
VM vm;

void initVM() {
    resetStack();
}

void freeVM() {
    
}

InterpretResult interpret(const char* source) {
    Chunk chunk;
    initChunk(&chunk);
    if (!compile(source, &chunk)) {
        freeChunk(&chunk);
        return INTERPRET_COMPILE_ERROR;
    }
    vm.chunk = &chunk;
    vm.ip = vm.chunk->code;
    InterpretResult result = run();
    freeChunk(&chunk);
    return result;
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop() {
    vm.stackTop--;
    return *vm.stackTop;
}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define READ_LONG_CONSTANT(index) (vm.chunk->constants.values[index])
// We do not pop and push into the stack the second operator to be more efficient.
#define BINARY_OP(valueType, op) \
    do { \
        if (!IS_NUMBER(peek(0)) || !IS_NUMBER(peek(1))) { \
            runtimeError("Operands must be numbers."); \
            return INTERPRET_RUNTIME_ERROR; \
        } \
        double b = AS_NUMBER(pop()); \
        double a = AS_NUMBER(pop()); \
        push(valueType(a op b)); \
    } while (false)

    while (true) {
#ifdef DEBUG_TRACE_EXECUTION
        printf("        ");
        for (Value* slot = vm.stack; slot < vm.stackTop; ++slot) {
            printf("[");
            printValue(*slot);
            printf("]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_CONSTANT_LONG: {
                uint32_t constantIndex = READ_BYTE();
                constantIndex |= (READ_BYTE() << 8);
                constantIndex |= (READ_BYTE() << 16);
                Value constant = READ_LONG_CONSTANT(constantIndex);
                push(constant);
                break;
            }
            case OP_NIL: push(NIL_VAL); break;
            case OP_TRUE: push(BOOL_VAL(true)); break;
            case OP_FALSE: push(BOOL_VAL(false)); break;
            case OP_EQUAL: {
                Value b = pop();
                Value a = pop();
                push(BOOL_VAL(valuesEqual(a, b)));
                break;
            }
            case OP_NEGATE: 
                if (!IS_NUMBER(peek(0))) {
                    runtimeError("Operand must be a number.");
                    return INTERPRET_RUNTIME_ERROR;
                }
                push(NUMBER_VAL(-AS_NUMBER(pop())));
                break;
            case OP_GREATER:    BINARY_OP(NUMBER_VAL, >); break;
            case OP_LESS:       BINARY_OP(NUMBER_VAL, <); break;
            case OP_ADD:        BINARY_OP(NUMBER_VAL, +); break;
            case OP_SUBTRACT:   BINARY_OP(NUMBER_VAL, -); break;
            case OP_MULTIPLY:   BINARY_OP(NUMBER_VAL, *); break;
            case OP_DIVIDE:     BINARY_OP(NUMBER_VAL, /); break;
            case OP_NOT: 
                push(BOOL_VAL(isFalsey(pop()))); 
                break;
            case OP_RETURN: {
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
            }
        } 
    }

#undef BINARY_OP
#undef READ_LONG_CONSTANT
#undef READ_CONSTANT
#undef READ_BYTE
}

static void resetStack() {
    vm.stackTop = vm.stack;
}

static Value peek(int distance) {
    return vm.stackTop[-1 -distance];
}

static void runtimeError(const char* format, ...) {
    va_list args;
    va_start(args, format);
    vfprintf(stderr, format, args);
    va_end(args);
    fputs("\n", stderr);
    size_t instruction = vm.ip - vm.chunk->code - 1;
    int line = vm.chunk->lines[instruction].line;
    fprintf(stderr, "[line %d] in script\n", line);
    resetStack();
}

static bool isFalsey(Value value) {
    return (IS_NIL(value)) || (IS_BOOL(value) && !AS_BOOL(value));
}

