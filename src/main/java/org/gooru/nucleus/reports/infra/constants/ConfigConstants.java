package org.gooru.nucleus.reports.infra.constants;

/**
 * Created by ashish on 26/4/16.
 */
public final class ConfigConstants {
    public static final String HTTP_PORT = "http.port";
    public static final String METRICS_PERIODICITY = "metrics.periodicity.seconds";
    public static final String MBUS_TIMEOUT = "message.bus.send.timeout.seconds";
    public static final String MAX_REQ_BODY_SIZE = "request.body.size.max.mb";
    
    public static final String ANALYTICS = "analytics";
    public static final String EVENTS = "events";
    public static final String ARCHIVE = "archive";
    
    public static final String CASSANDRA_CLUSTER = "cassandra.cluster";
    public static final String CASSANDRA_KEYSAPCE = "cassandra.keyspace";
    public static final String CASSANDRA_SEEDS = "cassandra.seeds";
    
    public static final String FILE_DOWNLOAD_APP_URL = "insights.file.app.path";
    public static final String FILE_SAVE_REAL_PATH = "insights.file.real.path";
    
    public static final String DOUBLE_QUOTES = "\"";
    public static final String SLASH = "/";
    public static final String STRING_EMPTY= "";
	public static final String COMMA = ",";
    public static final String HYPHEN = "-";
    public static final String TILDA = "~";
    public static final String RS = "RS";
    public static final String KEY = "key";
    public static final String COLUMN_1 = "column1";
    public static final String VALUE = "value";
    public static final String COLLECTION = "collection";
    public static final String CLASS = "class";
    public static final String COURSE = "course";
    public static final String UNIT = "unit";
    public static final String LESSON = "lesson";
    public static final String ASSESSMENT = "assessment";
    public static final String COLUMNS_TO_EXPORT = "score_in_percentage|time_spent|views";
	public static final String RESOURCE_COLUMNS_TO_EXPORT = ".*question_status.*|.*score_in_percentage.*|.*time_spent.*|.*views.*";
	public static final String STRING_COLUMNS = ".*collection_type.*";
	public static final String BIGINT_COLUMNS = ".*score_in_percentage.*|.*time_spent.*|.*views.*";
	public static final String DATA = "data";
	public static final String TITLE = "title";
	public static final String NA = "NA";
	public static final String STATUS = "status";
	public static final String NOT_AVAILABLE = "Not_Available";
	public static final String IN_PROGRESS = "in-progress";
	public static final String COMPLETED = "completed";
	public static final String URL = "url";
	public static final String CSV_EXT = ".csv";
	public static final String ZIP_EXT = ".zip";
	
    private ConfigConstants() {
        throw new AssertionError();
    }

}
