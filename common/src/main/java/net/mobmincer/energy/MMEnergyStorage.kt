package net.mobmincer.energy

interface MMEnergyStorage {

    /**
     * Return false if calling [.insert] will absolutely always return 0, or true otherwise or in doubt.
     *
     *
     * Note: This function is meant to be used by cables or other devices that can transfer energy to know if
     * they should interact with this storage at all.
     */
    val supportsInsertion: Boolean
        get() = true

    /**
     * Try to insert up to some amount of energy into this storage.
     *
     * @param maxAmount The maximum amount of energy to insert. May not be negative.
     * @return A nonnegative integer not greater than maxAmount: the amount that was inserted.
     */
    fun insert(maxAmount: Long): Long

    /**
     * Return false if calling [.extract] will absolutely always return 0, or true otherwise or in doubt.
     *
     *
     * Note: This function is meant to be used by cables or other devices that can transfer energy to know if
     * they should interact with this storage at all.
     */
    val supportsExtraction: Boolean
        get() = true

    /**
     * Try to extract up to some amount of energy from this storage.
     *
     * @param maxAmount The maximum amount of energy to extract. May not be negative.
     * @return A nonnegative integer not greater than maxAmount: the amount that was extracted.
     */
    fun extract(maxAmount: Long): Long

    /**
     * Return the current amount of energy that is stored.
     */
    var energy: Long

    /**
     * Return the maximum amount of energy that could be stored.
     */
    fun getEnergyCapacity(): Long

    val isEmpty: Boolean
        get() = energy <= 0
}
