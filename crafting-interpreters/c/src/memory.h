#ifndef clox_memory_h
#define clox_memory_h

#include "common.h"
#include "object.h"

/**
 * Allocates the given amout of memeory for the requested type.
 */
#define ALLOCATE(type, count) \
    (type*)reallocate(NULL, 0, sizeof(type) * (count))

/**
 * Frees the memeory given a pointer and its type.
 */
#define FREE(type, pointer) reallocate(pointer, sizeof(type), 0)

/**
 * Calculate a new capacity given the current one.
 * In order to get oprimised performance it scales based on the old size.
 */
#define GROW_CAPACITY(capacity) \
    ((capacity) < 8 ? 8 : (capacity) * 2)

/**
 * Reallocate an array with newCount as the new capacity.
 */
#define GROW_ARRAY(type, pointer, oldCount, newCount) \
    (type*)reallocate(pointer, sizeof(type) * (oldCount), sizeof(type) * (newCount))

/**
 * Free the memeory pointed by pointer.
 */
#define FREE_ARRAY(type, pointer, oldCount) \
    reallocate(pointer, sizeof(type) * (oldCount), 0)

/**
 * This is the only function which deals with dynamic memory management in clox.
 * This way it is simpler to impement a GC.
 * The behaviour of this function is defined as:
 *  oldSize | newSize   | Operation
 * -------------------------------
 *  0       | != 0      | Allocate a new block
 *  != 0    | 0         | Free allocation
 *  != 0    | < oldSize | Shrink existing allocation
 *  != 0    | > oldSize | Grow existing allocation
 */
void* reallocate(void* pointer, size_t oldSize, size_t newSize);

void markObject(Obj* obj);
void markValue(Value value);
/**
 * Starts the GC.
 */
void collectGarbage();
/**
 * Frees the memeory associated to the allocated objects in the VM.
 */
void freeObjects();

#endif