package org.gooru.nucleus.reports.infra.downlod.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;
import org.gooru.nucleus.reports.infra.component.UtilityManager;
import org.gooru.nucleus.reports.infra.constants.ConfigConstants;
import org.gooru.nucleus.reports.infra.constants.ExportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.TypeCodec;

import io.vertx.core.json.JsonObject;

public class ClassExportServiceImpl implements ClassExportService {
	
	CSVFileGenerator csvFileGenerator = new CSVFileGenerator();
	ZipFileGenerator zipFileGenerator = new ZipFileGenerator();
	private CqlCassandraDao cqlDAO = CqlCassandraDao.instance();
	
	private static UtilityManager um = UtilityManager.getInstance();
	
	protected final Logger LOG = LoggerFactory.getLogger(ClassExportServiceImpl.class);

	@Override
	public JsonObject exportCsv(String classId, String courseId, String userId, String zipFileName) {
		try {
			if (StringUtils.isBlank(zipFileName)) {
				zipFileName = classId;
			}
			JsonObject result = new JsonObject();
			LOG.info("FileName : " + um.getFileSaveRealPath() + zipFileName + ConfigConstants.ZIP_EXT);
			ZipOutputStream zip = zipFileGenerator.createZipFile(um.getFileSaveRealPath() + zipFileName + ConfigConstants.ZIP_EXT);
			this.export(classId, courseId, null, null, null, ConfigConstants.COURSE, userId, zipFileName,zip);
			for (String unitId : getCollectionItems(courseId)) {
				LOG.info("	unit : " + unitId);
				this.export(classId, courseId, unitId, null, null, ConfigConstants.UNIT, userId, zipFileName,zip);
				for (String lessonId : getCollectionItems(unitId)) {
					LOG.info("		lesson : " + lessonId);
					this.export(classId, courseId, unitId, lessonId, null, ConfigConstants.LESSON, userId, zipFileName, zip);
					/*
					 * for(String assessmentId : getCollectionItems(lessonId)){
					 * LOG.info("			assessment : " + assessmentId);
					 * this.export(classId, courseId,unitId, lessonId,
					 * assessmentId, ConfigConstants.COLLECTION,
					 * userId,zipFileName); }
					 */
				}
			}
			
			um.getCacheMemory().put(zipFileName, ConfigConstants.COMPLETED);
			LOG.info("CSV generation completed...........");
			//result.put(ConfigConstants.URL, um.getDownloadAppUrl() + zipFileName +ConfigConstants.ZIP_EXT);
			result.put(ConfigConstants.STATUS, ConfigConstants.COMPLETED);
			zip.closeEntry();
			zip.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	private JsonObject export(String classId, String courseId, String unitId, String lessonId, String collectionId,
			String type, String userId, String zipFileName, ZipOutputStream zip) {
		JsonObject result = new JsonObject();
		try {
			result.put(ConfigConstants.STATUS, ConfigConstants.IN_PROGRESS);
			List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
			List<String> classMembersList = getClassMembersList(classId, userId);
			String leastId = getLeastId(courseId, unitId, lessonId, collectionId, type);
			String leastTitle = getContentTitle(leastId);
			LOG.info("leastId : " + leastId + " - leastTitle:" + leastTitle);
			List<String> collectionItemsList = getCollectionItems(leastId);

			for (String studentId : classMembersList) {
				Map<String, Object> dataMap = getDataMap();
				setUserDetails(dataMap, studentId);
				ResultSet usageDataSet = null;
				if (type.equalsIgnoreCase(ConfigConstants.COLLECTION)) {
					String rowKey = getSessionId(appendTilda(ConfigConstants.RS, classId, courseId, unitId, lessonId,
							collectionId, studentId));
					usageDataSet = cqlDAO.getArchievedSessionData(rowKey);
				}
				for (String collectionItemId : collectionItemsList) {
					String title = getContentTitle(collectionItemId);
					if (type.equalsIgnoreCase(ConfigConstants.COLLECTION)) {
						setDefaultResourceUsage(title, dataMap);
						if (usageDataSet != null) {
							processResultSet(usageDataSet, true, ConfigConstants.RESOURCE_COLUMNS_TO_EXPORT, dataMap,
									title, null);
						}
					} else {
						String usageRowKey = appendTilda(classId, courseId, unitId, lessonId, collectionItemId,
								studentId);
						setDefaultUsage(title, dataMap);
						setUsageData(dataMap, title, usageRowKey, ConfigConstants.COLLECTION);
						setUsageData(dataMap, title, usageRowKey, ConfigConstants.ASSESSMENT);
					}
				}
				dataList.add(dataMap);
			}
			LOG.info("CSV generation started...........");
			String csvName = zipFileName+ConfigConstants.SLASH+type+ConfigConstants.SLASH+appendHyphen(leastTitle, ConfigConstants.DATA);
			String folderName = zipFileName+ConfigConstants.SLASH+type;
			LOG.info("csvName:" + csvName);
			csvFileGenerator.generateCSVReport(true,folderName,csvName, dataList);
			zipFileGenerator.addFileInZip(csvName, zip);
		} catch (Exception e) {
			LOG.error("Exception while generating CSV", e);
		}
		return result;
	}

	private String getLeastId(String courseId, String unitId, String lessonId, String collectionId, String type) {
		String leastId = courseId;
		if (type.equalsIgnoreCase(ConfigConstants.COURSE)) {
			leastId = courseId;
		} else if (type.equalsIgnoreCase(ConfigConstants.UNIT)) {
			leastId = unitId;
		} else if (type.equalsIgnoreCase(ConfigConstants.LESSON)) {
			leastId = lessonId;
		} else if (type.equalsIgnoreCase(ConfigConstants.COLLECTION)) {
			leastId = collectionId;
		}
		return leastId;
	}

	private List<String> getClassMembersList(String classId, String userId) {
		List<String> classMembersList = null;
		if (StringUtils.isBlank(userId)) {
			classMembersList = getClassMembers(classId);
		} else {
			classMembersList = new ArrayList<String>();
			classMembersList.add(userId);
		}
		return classMembersList;
	}

	private List<String> getCollectionItems(String contentId) {
		List<String> collectionItems = new ArrayList<String>();
		ResultSet collectionItemSet = cqlDAO.getArchievedCollectionItem(contentId);
		for (Row collectionItemRow : collectionItemSet) {
			collectionItems.add(collectionItemRow.getString(ConfigConstants.COLUMN_1));
		}
		return collectionItems;
	}

	private List<String> getClassMembers(String classId) {
		List<String> classMembersList = new ArrayList<String>();
		ResultSet classMemberSet = cqlDAO.getArchievedClassMembers(classId);
		for (Row collectionItemRow : classMemberSet) {
			classMembersList.add(collectionItemRow.getString(ConfigConstants.COLUMN_1));
		}
		return classMembersList;
	}

	private void setUserDetails(Map<String, Object> dataMap, String userId) {
		ResultSet userDetailSet = cqlDAO.getArchievedUserDetails(userId);
		for (Row userDetailRow : userDetailSet) {
			dataMap.put(ExportConstants.FIRST_NAME, userDetailRow.getString("firstname"));
			dataMap.put(ExportConstants.LAST_NAME, userDetailRow.getString("lastname"));
		}
	}

	private String getContentTitle(String contentId) {
		String title = "";
		ResultSet contentDetails = cqlDAO.getArchievedContentTitle(contentId);
		for (Row contentDetailRow : contentDetails) {
			title = contentDetailRow.getString(ConfigConstants.TITLE);
		}
		return title;
	}

	private String getSessionId(String rowKey) {
		String sessionId = null;
		ResultSet sessionIdSet = cqlDAO.getArchievedCollectionRecentSessionId(rowKey);
		for (Row sessionIdRow : sessionIdSet) {
			sessionId = TypeCodec.varchar().deserialize(sessionIdRow.getBytes(ConfigConstants.VALUE),
					cqlDAO.getClusterProtocolVersion());
		}
		return sessionId;
	}

	private void setUsageData(Map<String, Object> dataMap, String title, String rowKey, String collectionType) {
		String columnNames = ConfigConstants.COLUMNS_TO_EXPORT;;
		boolean splitColumnName = false;
		ResultSet usageDataSet = cqlDAO.getArchievedClassData(appendTilda(rowKey, collectionType));
		processResultSet(usageDataSet, splitColumnName, columnNames, dataMap, title, collectionType);
	}

	private void processResultSet(ResultSet usageDataSet, boolean splitColumnName, String columnNames,Map<String, Object> dataMap, String title, String collectionType){
		for (Row usageDataRow : usageDataSet) {
			String dbColumnName = usageDataRow.getString(ConfigConstants.COLUMN_1);
			if (dbColumnName.matches(columnNames)) {
			String columnName = splitColumnName ? dbColumnName.split(ConfigConstants.TILDA)[1] : dbColumnName;
				Object value ;
				if(columnName.matches(ConfigConstants.BIGINT_COLUMNS)){
				value = TypeCodec.bigint().deserialize(usageDataRow.getBytes(ConfigConstants.VALUE),
						cqlDAO.getClusterProtocolVersion());
				}else{
					value = TypeCodec.varchar().deserialize(usageDataRow.getBytes(ConfigConstants.VALUE),
							cqlDAO.getClusterProtocolVersion());
				}
				dataMap.put(appendHyphen(title, collectionType, ExportConstants.csvHeaders(columnName)), value);
			}
		}
	}
	private Map<String, Object> getDataMap() {
		Map<String, Object> dataMap = new LinkedHashMap<String, Object>();
		dataMap.put(ExportConstants.FIRST_NAME, "");
		dataMap.put(ExportConstants.LAST_NAME, "");
		return dataMap;
	}

	private void setDefaultUsage(String title, Map<String, Object> dataMap) {
		dataMap.put(appendHyphen(title, ConfigConstants.COLLECTION, ExportConstants.VIEWS), 0);
		dataMap.put(appendHyphen(title, ConfigConstants.ASSESSMENT, ExportConstants.VIEWS), 0);
		dataMap.put(appendHyphen(title, ConfigConstants.COLLECTION, ExportConstants.TIME_SPENT), 0);
		dataMap.put(appendHyphen(title, ConfigConstants.ASSESSMENT, ExportConstants.TIME_SPENT), 0);
		dataMap.put(appendHyphen(title, ConfigConstants.COLLECTION, ExportConstants.SCORE_IN_PERCENTAGE), 0);
		dataMap.put(appendHyphen(title, ConfigConstants.ASSESSMENT, ExportConstants.SCORE_IN_PERCENTAGE), 0);
	}

	private void setDefaultResourceUsage(String title, Map<String, Object> dataMap) {
		dataMap.put(appendHyphen(title, ExportConstants.VIEWS), 0);
		dataMap.put(appendHyphen(title, ExportConstants.TIME_SPENT), 0);
		dataMap.put(appendHyphen(title, ExportConstants.SCORE_IN_PERCENTAGE), 0);
		dataMap.put(appendHyphen(title, ExportConstants.ANSWER_STATUS), ConfigConstants.NA);
	}
	private String appendTilda(String... texts) {
		StringBuffer sb = new StringBuffer();
		for (String text : texts) {
			if (StringUtils.isNotBlank(text)) {
				if (sb.length() > 0) {
					sb.append(ConfigConstants.TILDA);
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}
	
	private String appendHyphen(String... texts) {
		StringBuffer sb = new StringBuffer();
		for (String text : texts) {
			if (StringUtils.isNotBlank(text)) {
				if (sb.length() > 0) {
					sb.append(ConfigConstants.HYPHEN);
				}
				sb.append(text);
			}
		}
		return sb.toString();
	}
}
