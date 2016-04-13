
package com.eightkdata.mongowp;

import com.eightkdata.mongowp.bson.BsonTimestamp;
import com.eightkdata.mongowp.bson.impl.DefaultBsonTimestamp;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedInts;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.threeten.bp.Instant;

/**
 * OpTime encompasses an Instant and a 64-bit Term
 * number. OpTime can be used to label every op in an oplog with a unique
 * identifier.
 */
public class OpTime implements Comparable<OpTime>, Serializable {

    public static final OpTime EPOCH = new OpTime(0, 0);
    private static final long serialVersionUID = 1L;

    private final int secs;
    private final int term;

    public OpTime(Instant instant) {
        this.secs = UnsignedInteger.valueOf(instant.getEpochSecond()).intValue();
        this.term = UnsignedInteger.valueOf(instant.getNano()).intValue();
    }

    public OpTime(Instant instant, UnsignedInteger term) {
        this.secs = UnsignedInteger.valueOf(instant.getEpochSecond()).intValue();
        this.term = term.intValue();
    }

    public OpTime(UnsignedInteger secs, UnsignedInteger term) {
        this.secs = secs.intValue();
        this.term = term.intValue();
    }

    public OpTime(BsonTimestamp timestamp) {
        this(timestamp.getSecondsSinceEpoch(), timestamp.getOrdinal());
    }

    /**
     *
     * @param secs secs since epoch, <b>used as an unsigned int</b>!
     * @param term arbitrary ordinal value, <b>used as an unsigned int</b>
     */
    public OpTime(int secs, int term) {
        this.secs = secs;
        this.term = term;
    }

    public UnsignedInteger getSecs() {
        return UnsignedInteger.fromIntBits(secs);
    }
    
    public UnsignedInteger getTerm() {
        return UnsignedInteger.fromIntBits(term);
    }

    public BsonTimestamp asBsonTimestamp() {
        return new DefaultBsonTimestamp(secs, term);
    }

    public long toEpochMilli() {
        return UnsignedInts.toLong(secs) * 1000;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.secs;
        hash = 59 * hash + this.term;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpTime other = (OpTime) obj;
        if (this.secs != other.secs) {
            return false;
        }
        if (this.term != other.term) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(OpTime o) {
        int diff = UnsignedInts.compare(secs, o.secs);
        if (diff != 0) {
            return diff;
        }
        return UnsignedInts.compare(term, o.term);
    }

    public final boolean isAfter(@Nonnull OpTime other) {
        return compareTo(other) > 0;
    }

    public final boolean isEqualOrAfter(@Nonnull OpTime other) {
        return compareTo(other) >= 0;
    }

    public final boolean isBefore(@Nonnull OpTime other) {
        return compareTo(other) < 0;
    }

    public final boolean isEqualOrBefore(@Nonnull OpTime other) {
        return compareTo(other) <= 0;
    }

    @Override
    public String toString() {
        return "{t: " + UnsignedInteger.fromIntBits(secs) + ", i: "+ UnsignedInteger.fromIntBits(term) + "}";
    }

    /**
     * Returns this optime as a long where the first 32 bits corresponds to {@link #getSecs() } and
     * the last to {@link #getTerm()}.
     *
     * This might not be the safest or easier way to manipulate a optime, but it is the most compact
     * way to store a optime.
     *
     * @return this optime as a long where the first 32 bits corresponds to {@link #getSecs() } and
     *         the last to {@link #getTerm() }
     */
    public Long asLong() {
        final long INT_MASK = 0xffffffffL;
        return (secs & INT_MASK) << 32 | (term & INT_MASK);
    }
}
