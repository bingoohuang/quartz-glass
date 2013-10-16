package org.n3r.quartz.glass.log.joblog.jdbc;

import org.n3r.quartz.glass.configuration.Configuration;
import org.n3r.quartz.glass.log.joblog.JobLog;
import org.n3r.quartz.glass.log.joblog.JobLogLevel;
import org.n3r.quartz.glass.log.joblog.JobLogStore;
import org.n3r.quartz.glass.util.Page;
import org.n3r.quartz.glass.util.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcJobLogStore implements JobLogStore {
    private static final String TABLE_SUFFIX = "job_log";

    private NamedParameterJdbcTemplate jdbcTemplate;

    private Configuration configuration;

    public JdbcJobLogStore(DataSource dataSource, Configuration configuration) {
        this.configuration = configuration;
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void add(JobLog jobLog) {
        String sql = "insert into " + getTableName() +
                " (id, executionId, logLevel, logDate, jobClass, jobGroup, jobName, triggerGroup, triggerName, message, stackTrace, rootCause)" +
                " values (" + configuration.getTablePrefix() + "sequence.nextval, :executionId, :logLevel, :logDate, :jobClass, :jobGroup, :jobName, :triggerGroup, :triggerName, :message, :stackTrace, :rootCause)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("executionId", jobLog.getExecutionId())
                .addValue("logLevel", jobLog.getLevel().name())
                .addValue("logDate", jobLog.getDate())
                .addValue("jobClass", jobLog.getJobClass())
                .addValue("jobGroup", jobLog.getJobGroup())
                .addValue("jobName", jobLog.getJobName())
                .addValue("triggerGroup", jobLog.getTriggerGroup())
                .addValue("triggerName", jobLog.getTriggerName())
                .addValue("message", jobLog.getMessage())
                .addValue("stackTrace", jobLog.getStackTrace())
                .addValue("rootCause", jobLog.getRootCause());

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Page<JobLog> getLogs(Query query) {
        String sql = "from " + getTableName();

        return getLogs(sql, new MapSqlParameterSource(), query);
    }

    @Override
    public Page<JobLog> getLogs(Long executionId, Query query) {
        String sql = "from " + configuration.getTablePrefix() + "log where executionId = :executionId";

        SqlParameterSource source = new MapSqlParameterSource().addValue("executionId", executionId);

        return getLogs(sql, source, query);
    }

    @Override
    public synchronized void clear() {
        String sql = "truncate table " + getTableName();

        jdbcTemplate.getJdbcOperations().execute(sql);
    }

    private Page<JobLog> getLogs(String sqlBase, SqlParameterSource params, Query query) {
        String sql = query.applySqlLimit("select * " + sqlBase + " order by logDate asc");

        List<JobLog> jobLogs = jdbcTemplate.query(sql, params, new RowMapper<JobLog>() {
            @Override
            public JobLog mapRow(ResultSet rs, int rowNum) throws SQLException {
                return doMapRow(rs, rowNum);
            }
        });

        String countSql = "select count(*) " + sqlBase;

        Page<JobLog> page = Page.fromQuery(query);

        page.setItems(jobLogs);
        page.setTotalCount(jdbcTemplate.queryForObject(countSql, params, Integer.class));

        return page;
    }

    private List<JobLog> getLogs(String sqlBase, SqlParameterSource params) {
        String sql = "select * " + sqlBase + " order by logDate asc";

        return jdbcTemplate.query(sql, params, new RowMapper<JobLog>() {
            @Override
            public JobLog mapRow(ResultSet rs, int rowNum) throws SQLException {
                return doMapRow(rs, rowNum);
            }
        });
    }

    private JobLog doMapRow(ResultSet rs, int rowNum) throws SQLException {
        JobLog jobLog = new JobLog();

        jobLog.setExecutionId(rs.getLong("executionId"));
        jobLog.setLevel(JobLogLevel.valueOf(rs.getString("logLevel")));
        jobLog.setDate(rs.getTimestamp("logDate"));
        jobLog.setJobClass(rs.getString("jobClass"));
        jobLog.setJobGroup(rs.getString("jobGroup"));
        jobLog.setJobName(rs.getString("jobName"));
        jobLog.setTriggerGroup(rs.getString("triggerGroup"));
        jobLog.setTriggerName(rs.getString("triggerName"));
        jobLog.setMessage(rs.getString("message"));
        jobLog.setStackTrace(rs.getString("stackTrace"));
        jobLog.setRootCause(rs.getString("rootCause"));

        return jobLog;
    }

    private String getTableName() {
        return configuration.getTablePrefix() + TABLE_SUFFIX;
    }
}
