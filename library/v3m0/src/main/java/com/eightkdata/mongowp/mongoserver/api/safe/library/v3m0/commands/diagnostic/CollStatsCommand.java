package com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic;

import com.eightkdata.mongowp.mongoserver.api.safe.impl.AbstractCommand;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic.CollStatsCommand.CollStatsArgument;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.diagnostic.CollStatsCommand.CollStatsReply;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonDocumentBuilder;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonField;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonReaderTool;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.BadValueException;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.NoSuchKeyException;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.TypesMismatchException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.bson.BsonDocument;
import org.bson.BsonInt32;

/**
 *
 */
public class CollStatsCommand extends AbstractCommand<CollStatsArgument, CollStatsReply> {

    public static final CollStatsCommand INSTANCE = new CollStatsCommand();

    private CollStatsCommand() {
        super("collStats");
    }

    @Override
    public boolean isSlaveOk() {
        return true;
    }

    @Override
    public Class<? extends CollStatsArgument> getArgClass() {
        return CollStatsArgument.class;
    }

    @Override
    public CollStatsArgument unmarshallArg(BsonDocument requestDoc) 
            throws TypesMismatchException, BadValueException, NoSuchKeyException {
        return CollStatsArgument.unmarshall(requestDoc);
    }

    @Override
    public BsonDocument marshallArg(CollStatsArgument request) {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Override
    public Class<? extends CollStatsReply> getResultClass() {
        return CollStatsReply.class;
    }

    @Override
    public BsonDocument marshallResult(CollStatsReply reply) {
        return reply.marshall();
    }

    @Override
    public CollStatsReply unmarshallResult(BsonDocument resultDoc) throws
            BadValueException, TypesMismatchException, NoSuchKeyException {
        throw new UnsupportedOperationException("Not supported yet."); //TODO
    }

    @Immutable
    public static class CollStatsArgument {

        private static final BsonField<String> COLLECTION_FIELD = BsonField.create("collStats");
        private static final BsonField<Number> SCALE_FIELD = BsonField.create("scale");
        private static final BsonField<Boolean> VERBOSE_FIELD = BsonField.create("verbose");
        private final String collection;
        private final int scale;
        private final boolean verbose;

        public CollStatsArgument(
                @Nonnull String collection,
                @Nonnegative int scale,
                boolean verbose) {
            this.collection = collection;
            this.scale = scale;
            this.verbose = verbose;
        }

        @Nonnegative
        public int getScale() {
            return scale;
        }

        public boolean isVerbose() {
            return verbose;
        }

        protected static CollStatsArgument unmarshall(BsonDocument doc)
                throws TypesMismatchException, BadValueException, NoSuchKeyException {
            String collection = BsonReaderTool.getString(doc, COLLECTION_FIELD);
            int scale = BsonReaderTool.getNumeric(doc, SCALE_FIELD, new BsonInt32(1)).intValue();
            if (scale <= 0) {
                throw new BadValueException("Scale must be a value >= 1");
            }
            boolean verbose = BsonReaderTool.getBooleanOrNumeric(doc, VERBOSE_FIELD, false);

            return new CollStatsArgument(collection, scale, verbose);
        }

        public String getCollection() {
            return collection;
        }

    }

    //TODO(gortiz): This reply is not prepared to respond on error cases!
    public static class CollStatsReply {

        private static final BsonField<String> NS_FIELD = BsonField.create("ns");
        private static final BsonField<Number> COUNT_FIELD = BsonField.create("count");
        private static final BsonField<Number> SIZE_FIELD = BsonField.create("size");
        private static final BsonField<Number> AVG_OBJ_SIZE_FIELD = BsonField.create("avgObjSize");
        private static final BsonField<Number> STORAGE_SIZE_FIELD = BsonField.create("storageSize");
        private static final BsonField<Integer> N_INDEXES_FIELD = BsonField.create("nindexes");
        private static final BsonField<BsonDocument> INDEX_DETAILS_FIELD = BsonField.create("indexDetails");
        private static final BsonField<Number> TOTAL_INDEX_SIZE_FIELD = BsonField.create("totalIndexSize");
        private static final BsonField<BsonDocument> INDEX_SIZES_FIELD = BsonField.create("indexSizes");
        private static final BsonField<Boolean> CAPPED_FIELD = BsonField.create("capped");
        private static final BsonField<Number> MAX_FIELD = BsonField.create("max");

        private final int scale;
        private final @Nonnull String database;
        private final @Nonnull String collection;
        private final @Nonnull Number count;
        private final @Nonnull Number size;
        private final @Nonnull Number storageSize;
        private final @Nullable BsonDocument customStorageStats;
        private final boolean capped;
        private final @Nullable Number maxIfCapped;
        private final @Nonnull BsonDocument indexDetails;
        private final @Nonnull ImmutableMap<String, ? extends Number> sizeByIndex;

        public CollStatsReply(
                @Nonnegative int scale,
                String database,
                String collection,
                Number count,
                Number size,
                Number storageSize,
                @Nullable BsonDocument customStorageStats,
                boolean capped,
                Number maxIfCapped,
                BsonDocument indexDetails,
                ImmutableMap<String, ? extends Number> sizeByIndex) {
            this.scale = scale;
            this.database = database;
            this.collection = collection;
            this.count = count;
            this.size = size;
            this.storageSize = storageSize;
            this.customStorageStats = customStorageStats;
            this.capped = capped;
            this.maxIfCapped = maxIfCapped;
            this.indexDetails = indexDetails;
            this.sizeByIndex = sizeByIndex;
        }

        private BsonDocument marshall() {
            BsonDocumentBuilder builder = new BsonDocumentBuilder();
            builder.append(NS_FIELD, database + '.' + collection);
            builder.appendNumber(COUNT_FIELD, count);
            builder.appendNumber(SIZE_FIELD, size);
            if (count.longValue() != 0) {
                Number avgObjSize = scale * size.longValue() / count.longValue();
                builder.appendNumber(AVG_OBJ_SIZE_FIELD, avgObjSize);
            }
            builder.appendNumber(STORAGE_SIZE_FIELD, storageSize);
            builder.append(N_INDEXES_FIELD, sizeByIndex.size());
            builder.append(INDEX_DETAILS_FIELD, indexDetails);
            builder.appendNumber(TOTAL_INDEX_SIZE_FIELD, getTotalIndexSize());
            builder.append(INDEX_SIZES_FIELD, marshallSizeByIndex(sizeByIndex));
            builder.append(CAPPED_FIELD, capped);
            if (maxIfCapped != null) {
                builder.appendNumber(MAX_FIELD, maxIfCapped);
            }

            return builder.build();
        }

        private Number getTotalIndexSize() {
            long totalSize = 0;
            for (Number indexSize : sizeByIndex.values()) {
                totalSize += indexSize.longValue();
            }
            return totalSize;
        }

        private BsonDocument marshallSizeByIndex(ImmutableMap<String, ? extends Number> sizeByIndex) {
            BsonDocumentBuilder builder = new BsonDocumentBuilder();
            for (Entry<String, ? extends Number> entry : sizeByIndex.entrySet()) {
                builder.appendNumber(new BsonField<Number>(entry.getKey()), entry.getValue());
            }
            return builder.build();
        }

        public static class Builder {

            private int scale;
            private final @Nonnull String database;
            private final @Nonnull String collection;
            private Number count;
            private Number size;
            private Number storageSize;
            private @Nullable BsonDocument customStorageStats;
            private boolean capped;
            private @Nullable Number maxIfCapped;
            private BsonDocument indexDetails;
            private Map<String, ? extends Number> sizeByIndex;

            public Builder(@Nonnull String database, @Nonnull String collection) {
                this.database = database;
                this.collection = collection;
            }

            public int getScale() {
                return scale;
            }

            public Builder setScale(int scale) {
                Preconditions.checkArgument(scale > 0, "Scale must be a positive number");
                this.scale = scale;
                return this;
            }

            public Number getCount() {
                return count;
            }

            /**
             *
             * @param count The number of objects or documents in this collection.
             * @return
             */
            public Builder setCount(@Nonnull @Nonnegative Number count) {
                this.count = count;
                return this;
            }

            public Number getSize() {
                return size;
            }

            /**
             * The total size of all records in a collection. This value does not
             * include the record header, which is 16 bytes per record, but does
             * include the record’s padding. Additionally size does not include the
             * size of any indexes associated with the collection.
             * <p>
             * The scale argument affects this value.
             * <p>
             * @param size
             * @return
             */
            public Builder setSize(@Nonnull @Nonnegative Number size) {
                this.size = size;
                return this;
            }

            public Number getStorageSize() {
                return storageSize;
            }

            /**
             * The total amount of storage allocated to this collection for document
             * storage. The scale argument affects this value. The storageSize does
             * not decrease as you remove or shrink documents.
             * <p>
             * @param storageSize
             * @return
             */
            public Builder setStorageSize(@Nonnull @Nonnegative Number storageSize) {
                this.storageSize = storageSize;
                return this;
            }

            public boolean isCapped() {
                return capped;
            }

            /**
             * This field will be “true” if the collection is capped.
             * <p>
             * @param capped
             * @return
             */
            public Builder setCapped(boolean capped) {
                this.capped = capped;
                return this;
            }

            public Number getMaxIfCapped() {
                return maxIfCapped;
            }

            /**
             * Shows the maximum number of documents that may be present in a capped
             * collection.
             * <p>
             * @param maxIfCapped
             * @return
             */
            public Builder setMaxIfCapped(@Nullable @Nonnegative Number maxIfCapped) {
                this.maxIfCapped = maxIfCapped;
                return this;
            }

            public Map<String, ? extends Number> getSizeByIndex() {
                return sizeByIndex;
            }

            /**
             * This field specifies the key and size of every existing index on the
             * collection. The scale argument affects this value.
             * <p>
             * @param sizeByIndex
             * @return
             */
            public Builder setSizeByIndex(@Nonnull Map<String, ? extends Number> sizeByIndex) {
                this.sizeByIndex = sizeByIndex;
                return this;
            }

            public BsonDocument getCustomStorageStats() {
                return customStorageStats;
            }

            public Builder setCustomStorageStats(@Nullable BsonDocument customStorageStats) {
                this.customStorageStats = customStorageStats;
                return this;
            }

            public BsonDocument getIndexDetails() {
                return indexDetails;
            }

            public Builder setIndexDetails(@Nonnull BsonDocument indexDetails) {
                this.indexDetails = indexDetails;
                return this;
            }

            public CollStatsReply build() {
                assert scale > 0;
                assert database != null;
                assert collection != null;
                assert count != null;
                assert size != null;
                assert storageSize != null;
                assert indexDetails != null;
                assert sizeByIndex != null;


                return new CollStatsReply(
                        scale,
                        database,
                        collection,
                        count,
                        size,
                        storageSize,
                        customStorageStats,
                        capped,
                        maxIfCapped,
                        indexDetails,
                        ImmutableMap.copyOf(sizeByIndex)
                );
            }
        }

    }

}

