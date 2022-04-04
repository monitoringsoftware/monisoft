package de.jmonitoring.Consistency;

/**
 *
 * @author togro
 */
public class LimitExceedance {

    private Long numberOfExceedancesUpper;
    private Long numberOfExceedancesLower;
    private Double maxExceedanceUpper;
    private Double maxExceedanceLower;
    private Integer lowerLimit;
    private Integer upperLimit;

    public LimitExceedance() {
        this.numberOfExceedancesLower = 0L;
        this.numberOfExceedancesUpper = 0L;
        this.maxExceedanceUpper = 0d;
        this.maxExceedanceLower = 0d;
        this.upperLimit = null;
        this.lowerLimit = null;
    }

    public Long getNumberOfExceedancesUpper() {
        return numberOfExceedancesUpper;
    }

    public void setNumberOfExceedancesUpper(Long numberOfExceedancesUpper) {
        this.numberOfExceedancesUpper = numberOfExceedancesUpper;
    }

    public Long getNumberOfExceedancesLower() {
        return numberOfExceedancesLower;
    }

    public void setNumberOfExceedancesLower(Long numberOfExceedancesLower) {
        this.numberOfExceedancesLower = numberOfExceedancesLower;
    }

    public Double getMaxExceedanceUpper() {
        return maxExceedanceUpper;
    }

    public void setMaxExceedanceUpper(Double maxExceedanceUpper) {
        this.maxExceedanceUpper = maxExceedanceUpper;
    }

    public Double getMaxExceedanceLower() {
        return maxExceedanceLower;
    }

    public void setMaxExceedanceLower(Double maxExceedanceLower) {
        this.maxExceedanceLower = maxExceedanceLower;
    }

    public Integer getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Integer lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Integer getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Integer upperLimit) {
        this.upperLimit = upperLimit;
    }
}
