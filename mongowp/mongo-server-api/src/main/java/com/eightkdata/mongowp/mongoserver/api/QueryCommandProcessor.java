/*
 *     This file is part of mongowp.
 *
 *     mongowp is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     mongowp is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with mongowp. If not, see <http://www.gnu.org/licenses/>.
 *
 *     Copyright (c) 2014, 8Kdata Technology
 *     
 */


package com.eightkdata.mongowp.mongoserver.api;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.google.common.base.Preconditions;

import com.eightkdata.mongowp.messages.request.RequestBaseMessage;
import com.eightkdata.mongowp.mongoserver.api.callback.MessageReplier;
import com.eightkdata.mongowp.mongoserver.api.commands.AdministrationQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.AggregationQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.AuthenticationQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.DiagnosticQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.GeospatialQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.InternalQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.QueryAndWriteOperationsQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.ReplicationQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.RoleManagementQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.ShardingQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.SystemEventsAuditingQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.TestingQueryCommand;
import com.eightkdata.mongowp.mongoserver.api.commands.UserManagementQueryCommand;
import com.eightkdata.nettybson.api.BSONDocument;
import java.util.Locale;

/**
 *
 */
public interface QueryCommandProcessor {
	public enum QueryCommandGroup {
		Aggregation(AggregationQueryCommand.values()),
		Geospatial(GeospatialQueryCommand.values()),
		QueryAndWriteOperations(QueryAndWriteOperationsQueryCommand.values()),
		Authentication(AuthenticationQueryCommand.values()),
		UserManagement(UserManagementQueryCommand.values()),
		RoleManagement(RoleManagementQueryCommand.values()),
		Replication(ReplicationQueryCommand.values()),
		Sharding(ShardingQueryCommand.values()),
		Administration(AdministrationQueryCommand.values()),
		Diagnostic(DiagnosticQueryCommand.values()),
		Internal(InternalQueryCommand.values()),
		Testing(TestingQueryCommand.values()),
		SystemEventsAuditing(SystemEventsAuditingQueryCommand.values());
		
		private final QueryCommand[] queryCommands;
		
		private QueryCommandGroup(QueryCommand[] queryCommands) {
			this.queryCommands = queryCommands;
		}

	    private static final Map<String,QueryCommand> COMMANDS_MAP = new HashMap<String, QueryCommand>();
	    static {
	        for(QueryCommandGroup commandGroup : values()) {
		        for(QueryCommand command : commandGroup.queryCommands) {
		        	// 	Some driver use lower case version of the command so we must take it into account
		        	String key = command.getKey().toLowerCase(Locale.ROOT);
		        	if (COMMANDS_MAP.containsKey(key)) {
		        		throw new RuntimeException("Key " + key + " is not unique, found in enum " + 
		        				COMMANDS_MAP.get(key).getClass().getName() + " and in enum " + 
		        				command.getClass().getName() + ". Fix it!");
		        	}
		        	COMMANDS_MAP.put(key, command);
		        }
	        }
	    }

	    /**
	     *
	     * @param queryDocument
	     * @return
	     * @throws IllegalArgumentException If queryDocument is null
	     */
	    @Nullable
	    public static QueryCommand byQueryDocument(@Nonnull BSONDocument queryDocument) {
	        Preconditions.checkNotNull(queryDocument);

	        // TODO: if might be worth to improve searching taking into account the # of keys of the document,
	        // matching it with the # of args of the commands, which could be registered as enum fields
	        for(String possibleCommand : queryDocument.getKeys()) {
	            // Some driver use lower case version of the command so we must take it into account
	        	possibleCommand = possibleCommand.toLowerCase(Locale.ROOT);
	            if(COMMANDS_MAP.containsKey(possibleCommand)) {
	                return COMMANDS_MAP.get(possibleCommand);
	            }
	        }

	        return null;
	    }
	}
	
    public interface QueryCommand {
        /**
         *
         * @param requestBaseMessage
         * @param query
         * @param caller
         * @throws java.lang.IllegalArgumentException If either query or caller are null
         */
        public void call(@Nonnull RequestBaseMessage requestBaseMessage, @Nonnull BSONDocument query, @Nonnull ProcessorCaller caller) throws Exception;
        
        public String getKey();
        
        public boolean isAdminOnly();
    }

    public class ProcessorCaller extends QueryCommandProcessorCaller {
        @Nonnull private final QueryCommandProcessor queryCommandProcessor;

        @Inject
        public ProcessorCaller(@Nonnull String database,
                @Nonnull QueryCommandProcessor queryCommandProcessor, @Nonnull MessageReplier messageReplier
        ) {
            super(database, messageReplier);
            this.queryCommandProcessor = queryCommandProcessor;
        }

		public void count(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.count(document, messageReplier);
        }
        
        public void insert(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.insert(document, messageReplier);
        }
        
        public void update(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.update(document, messageReplier);
        }
        
        public void delete(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.delete(document, messageReplier);
        }
        
        public void createIndexes(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.createIndexes(document, messageReplier);
        }
        
        public void create(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.create(document, messageReplier);
        }
        
        public void drop(@Nonnull BSONDocument document) throws Exception {
        	queryCommandProcessor.drop(document, messageReplier);
        }

        public void getLastError(
        		@Nullable Object w, boolean j, boolean fsync,
                @Nonnegative @Nullable int wtimeout
        ) throws Exception {
            queryCommandProcessor.getLastError(w, j, fsync, wtimeout, messageReplier);
        }
        
        public void validate(@Nonnull String database, @Nonnull BSONDocument document) {
        	queryCommandProcessor.validate(database, document, messageReplier);
        }
        
        public void whatsmyuri(@Nonnull String host, @Nonnull int port) {
        	queryCommandProcessor.whatsmyuri(host, port, messageReplier);
        }
        
        public void isMaster() {
        	queryCommandProcessor.isMaster(messageReplier);
        }
        
        public void replSetGetStatus() {
        	queryCommandProcessor.replSetGetStatus(messageReplier);
        }
        
        public void buildInfo() {
        	queryCommandProcessor.buildInfo(messageReplier);
        }
        
        public void getLog(@Nonnull GetLogType log) {
        	queryCommandProcessor.getLog(log, messageReplier);
        }

        public void unimplemented(@Nonnull QueryCommand userCommand) throws Exception {
        	queryCommandProcessor.unimplemented(userCommand, messageReplier);
        }
    }

    public void count(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;
    
    public void insert(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;

    public void update(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;
    
    public void delete(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;
    
    public void drop(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;

    public void createIndexes(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;
    
    public void create(@Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier) throws Exception;
    
    /**
     * Either w or majority will be set, but not both at the same time.
     * @param j
     * @param w
     * @param majority
     * @param fsync
     * @param wtimeout
     * @param messageReplier
     */
    public void getLastError(
    		@Nullable Object w, boolean j, boolean fsync,
            @Nonnegative @Nullable int wtimeout, @Nonnull MessageReplier messageReplier
    ) throws Exception;

	public void validate(@Nonnull String database, @Nonnull BSONDocument document, @Nonnull MessageReplier messageReplier);

    public void whatsmyuri(@Nonnull String host, @Nonnull int port, @Nonnull MessageReplier messageReplier);

    public void replSetGetStatus(@Nonnull MessageReplier messageReplier);

    public enum GetLogType {
        global("global"),
        rs("rs"),
        startupWarnings("startupWarnings"),
        all("*");

        private final String logFilter;

        GetLogType(String logFilter) {
            this.logFilter = logFilter;
        }

        public String getLogFilter() {
            return logFilter;
        }

        private static final Map<String,GetLogType> TYPES_MAP = new HashMap<String, GetLogType>(values().length);
        static {
            for(GetLogType getLogType : values()) {
                TYPES_MAP.put(getLogType.logFilter, getLogType);
            }
        }

        @Nullable public static GetLogType getByLog(String log) {
            return TYPES_MAP.get(log);
        }
    }
    public void getLog(@Nonnull GetLogType log, @Nonnull MessageReplier messageReplier);
    
    public void isMaster(@Nonnull MessageReplier messageReplier);
    
    public void buildInfo(@Nonnull MessageReplier messageReplier);
    
    public boolean handleError(@Nonnull QueryCommand userCommand, @Nonnull MessageReplier messageReplier, @Nonnull Throwable throwable)
			throws Exception;

    public void unimplemented(@Nonnull QueryCommand userCommand, @Nonnull MessageReplier messageReplier) throws Exception;
}