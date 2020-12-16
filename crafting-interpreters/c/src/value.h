#ifndef clox_value_h
#define clox_value_h

#include "common.h"

typedef struct Obj Obj;
typedef struct ObjString ObjString;

typedef enum {
    VAL_BOOL,
    VAL_NIL,
    VAL_NUMBER,
    VAL_OBJ
} ValueType;

/**
 * Rapresentation of a Lox's value, using union type to optimise the space.
 */
typedef struct {
    // The type flag.
    ValueType type;
    union {
        bool boolean;
        double number;
        Obj* obj;
    } as;
} Value;

/**
 * Checks the type of a Lox's value.
 */
#define IS_BOOL(value)      ((value).type == VAL_BOOL)
#define IS_NIL(value)       ((value).type == VAL_NIL)
#define IS_NUMBER(value)    ((value).type == VAL_NUMBER)
#define IS_OBJ(value)       ((value).type == VAL_OBJ)
/**
 * Access the contained value of a Lox's Value.c
 */
#define AS_BOOL(value)      ((value).as.boolean)
#define AS_NUMBER(value)    ((value).as.number)
#define AS_OBJ(value)       ((value).as.obj)
/**
 * Define a new Lox's value. In other words, take a C value and lift it to a Lox's one.
 */
#define BOOL_VAL(value)     ((Value){VAL_BOOL, {.boolean = value}})
#define NIL_VAL             ((Value){VAL_NIL, {.number = 0}})
#define NUMBER_VAL(value)   ((Value){VAL_NUMBER, {.number = value}})  
#define OBJ_VAL(value)      ((Value){VAL_OBJ, {.obj = (Obj*)value}})  

typedef struct {
    int capacity;
    int count;
    Value* values;
} ValueArray;

void initValueArray(ValueArray* array);
void writeValueArray(ValueArray* array, Value value);
void freeValueArray(ValueArray* array);

/**
 * Pretty prints a clox value.
 */
void printValue(Value value);
/**
 * Returns true if a == b.
 */
bool valuesEqual(Value a, Value b);


#endif