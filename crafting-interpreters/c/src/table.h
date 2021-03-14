#ifndef clox_table_h
#define clox_table_h

#include "common.h"
#include "value.h"

typedef struct {
    ObjString* key;
    Value value;
} Entry;

/**
 * Capacity: The allocated size of the array. 
 * Count: The number of key/value pairs currently stored in it. 
 * The ratio of count to capacity is exactly the load factor of the hash table.
 */
typedef struct {
    int count;
    int capacity;
    Entry* entries;
} Table;

void initTable(Table* table);
void freeTable(Table* table);

bool tableSet(Table* table, ObjString* key, Value value);
/**
 * Lookups key in table and if the key is found stores its value in value.
 * Returns false if the key is not in the table.
 */
bool tableGet(Table* table, ObjString* key, Value* value);
bool tableDelete(Table* table, ObjString* key);
void tableAddAll(Table* from, Table* to);
/**
 * Finds a String 
 */
ObjString* tableFindString(Table* table, const char* chars, int length, uint32_t hash);

void tableRemoveWhite(Table* table);
void markTable(Table* table);

#endif