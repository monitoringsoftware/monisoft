package de.jmonitoring.help;

/**
 * This enum holds bookmarks/topics for the pdf-manual.<p> The page number for
 * each topic is given in paranthesis.<br> These page numbers are the real page
 * number of the document.
 *
 * @author togro
 */
public enum ManualBookmarks {

    PASSWORD_DIALOG(11),
    NEW_PROJECT(19),
    MAIN_FRAME(27),
    SENSOR_EDITOR(31),
    VIRTUAL_SENSORS(32),
    UNIT_DIALOG(34),
    SENSOR_TABLE(35),
    SENSOR_COLLECTIONS(39),
    SENSOR_CATEGORYS(40),
    ANNOTATION_DESIGNER(63),
    FAVORITE_DIALOG(65),
    BUILDING_TABLE(68),
    BUILDING_EDITOR(69),
    DEFINE_REFERENCES(70),
    REFERENCES_TABLE(71),
    COMPARE_VALUE_MAPPING(72),
    BUILDING_PROFILE(75),
    CONSUMPTION_FRAME(77),
    SECTION_CHART(80),
    CLUSTER_EDITOR(82),
    CLUSTER_SORT(83),
    CLUSTER_MATRIX(84),
    COMPARE_TABLE(87),
    STATISTICS_FRAME(88),
    DATA_QUALITY(88),
    DATA_CHECK(91),
    FACTOR_CHANGE(99),
    COUNTER_CHANGE(100),
    MONTHLY_PANEL(104),
    MONTHLY_CALC(104),
    CSV_IMPORT(107),
    MANUAL_DATA_ENTRY(111),
    CSV_EXPORT(112),
    DELETE_DATA_DIALOG(114),
    APP_PREFS_DIALOG(115);
    // 
    private final int page;

    ManualBookmarks(int page) {
        this.page = page;
    }

    /**
     * Returns the page number-1 because the internal counting begins with 0.
     *
     * @return
     */
    public int getPage() {
        return page - 1;
    }
}
