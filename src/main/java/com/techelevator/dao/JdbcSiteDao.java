package com.techelevator.dao;

import com.techelevator.model.Site;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcSiteDao implements SiteDao {

    private JdbcTemplate jdbcTemplate;

    public JdbcSiteDao(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Site> getSitesThatAllowRVs(int parkId)
    {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site_id, campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities " +
                    "FROM site " +
                    "JOIN campground USING(campground_id) " +
                    "JOIN park USING(park_id) " +
                    "WHERE park_id = ? AND (accessible = true and max_rv_length > 0);";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId);
        while(results.next()) {
            sites.add(mapRowToSite(results));
        }

        return sites;
    }

    @Override
    public List<Site> getAvailableSites(int parkId) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site_id, campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities " +
                "FROM reservation " +
                "JOIN site USING(site_id) " +
                "JOIN campground USING(campground_id) " +
                "JOIN park USING(park_id) " +
                "WHERE park_id = ? AND (CURRENT_DATE NOT BETWEEN from_date AND to_date) AND from_date < to_date;";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId);
        while(results.next()) {
            sites.add(mapRowToSite(results));
        }

        return sites;
    }

    @Override
    public List<Site> getAvailableSitesDateRange(int parkId, LocalDate fromDate, LocalDate toDate) {
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT site_id, campground_id, site_number, max_occupancy, accessible, max_rv_length, utilities " +
                "FROM site " +
                "JOIN reservation USING(site_id) " +
                "JOIN campground USING(campground_id) " +
                "JOIN park USING(park_id) " +
                "WHERE park_id = ? AND from_date < to_date AND (? NOT BETWEEN from_date AND to_date) AND " +
                "(? NOT BETWEEN from_date AND to_date);";
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, parkId, fromDate, toDate);
        while(results.next()) {
            sites.add(mapRowToSite(results));
        }
        return sites;
    }

    private Site mapRowToSite(SqlRowSet results) {
        Site site = new Site();
        site.setSiteId(results.getInt("site_id"));
        site.setCampgroundId(results.getInt("campground_id"));
        site.setSiteNumber(results.getInt("site_number"));
        site.setMaxOccupancy(results.getInt("max_occupancy"));
        site.setAccessible(results.getBoolean("accessible"));
        site.setMaxRvLength(results.getInt("max_rv_length"));
        site.setUtilities(results.getBoolean("utilities"));
        return site;
    }
}
