
package com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.admin;

import com.eightkdata.mongowp.mongoserver.api.safe.MarshalException;
import com.eightkdata.mongowp.mongoserver.api.safe.impl.AbstractCommand;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.admin.ListIndexesCommand.ListIndexesArgument;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.commands.admin.ListIndexesCommand.ListIndexesResult;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.pojos.IndexOptions;
import com.eightkdata.mongowp.mongoserver.api.safe.library.v3m0.tools.CursorMarshaller;
import com.eightkdata.mongowp.mongoserver.api.safe.pojos.MongoCursor;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonDocumentBuilder;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonField;
import com.eightkdata.mongowp.mongoserver.api.safe.tools.bson.BsonReaderTool;
import com.eightkdata.mongowp.mongoserver.protocol.exceptions.*;
import javax.annotation.Nonnull;
import org.bson.BsonDocument;

/**
 *
 */
public class ListIndexesCommand extends AbstractCommand<ListIndexesArgument, ListIndexesResult>{

    public static final ListIndexesCommand INSTANCE = new ListIndexesCommand();
    private static final String COMMAND_NAME = "listIndexes";

    private ListIndexesCommand() {
        super(COMMAND_NAME);
    }

    @Override
    public Class<? extends ListIndexesArgument> getArgClass() {
        return ListIndexesArgument.class;
    }

    @Override
    public ListIndexesArgument unmarshallArg(BsonDocument requestDoc) 
            throws TypesMismatchException, NoSuchKeyException, BadValueException {
        return ListIndexesArgument.unmarshall(requestDoc);
    }

    @Override
    public BsonDocument marshallArg(ListIndexesArgument request) {
        return request.marshall(request);
    }

    @Override
    public Class<? extends ListIndexesResult> getResultClass() {
        return ListIndexesResult.class;
    }

    @Override
    public BsonDocument marshallResult(ListIndexesResult reply) throws MarshalException{
        try {
            return reply.marshall();
        } catch (MongoException ex) {
            throw new MarshalException(ex);
        }
    }

    @Override
    public ListIndexesResult unmarshallResult(BsonDocument replyDoc)
            throws TypesMismatchException, NoSuchKeyException, BadValueException {
        return ListIndexesResult.unmarshall(replyDoc);
    }

    public static class ListIndexesArgument {

        private static final BsonField<String> COL_NAME_FIELD = BsonField.create("listIndexes");
        private final String collection;

        public ListIndexesArgument(String collection) {
            this.collection = collection;
        }

        @Nonnull
        public String getCollection() {
            return collection;
        }

        private static ListIndexesArgument unmarshall(BsonDocument requestDoc) 
                throws TypesMismatchException, NoSuchKeyException, BadValueException {
            try {
                String colName = BsonReaderTool.getString(requestDoc, COL_NAME_FIELD);
                if (colName.isEmpty()) {
                    throw new BadValueException("Argument to listIndexes must be "
                            + "a collection name, not the empty string");
                }
                return new ListIndexesArgument(colName);
            } catch (TypesMismatchException ex) {
                throw ex.newWithMessage("Argument to listIndexes must be of "
                        + "type String, not " + ex.getFoundType());
            }
        }
        
        private BsonDocument marshall(ListIndexesArgument request) {
            return new BsonDocumentBuilder()
                    .append(COL_NAME_FIELD, collection)
                    .build();
        }

    }

    public static class ListIndexesResult {
        private static final BsonField<BsonDocument> CURSOR_FIELD = BsonField.create("cursor");
        private final MongoCursor<IndexOptions> cursor;

        public ListIndexesResult(MongoCursor<IndexOptions> cursor) {
            this.cursor = cursor;
        }

        public MongoCursor<IndexOptions> getCursor() {
            return cursor;
        }

        private static ListIndexesResult unmarshall(BsonDocument reply)
                throws TypesMismatchException, NoSuchKeyException, BadValueException {
            BsonDocument cursorDoc = BsonReaderTool.getDocument(reply, CURSOR_FIELD);

            return new ListIndexesResult(
                    CursorMarshaller.unmarshall(cursorDoc, IndexOptions.UNMARSHALLER_FUN)
            );
        }

        private BsonDocument marshall() throws MongoException {
            BsonDocumentBuilder builder = new BsonDocumentBuilder();

            return builder
                    .append(CURSOR_FIELD, CursorMarshaller.marshall(cursor, IndexOptions.MARSHALLER_FUN))
                    .build();
        }

    }
}
