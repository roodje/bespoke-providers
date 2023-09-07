package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Details of underlying standing orders. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public class StandingOrderDetails   {

  @JsonProperty("startDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate startDate;

  @JsonProperty("frequency")
  private FrequencyCode frequency;

  @JsonProperty("endDate")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate endDate;

  @JsonProperty("executionRule")
  private ExecutionRule executionRule;

  @JsonProperty("withinAMonthFlag")
  private Boolean withinAMonthFlag;

  /**
   * Gets or Sets monthsOfExecution
   */
  public enum MonthsOfExecutionEnum {
    _1("1"),
    
    _2("2"),
    
    _3("3"),
    
    _4("4"),
    
    _5("5"),
    
    _6("6"),
    
    _7("7"),
    
    _8("8"),
    
    _9("9"),
    
    _10("10"),
    
    _11("11"),
    
    _12("12");

    private String value;

    MonthsOfExecutionEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static MonthsOfExecutionEnum fromValue(String value) {
      for (MonthsOfExecutionEnum b : MonthsOfExecutionEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("monthsOfExecution")
  @Valid
  private List<MonthsOfExecutionEnum> monthsOfExecution = null;

  @JsonProperty("multiplicator")
  private Integer multiplicator;

  @JsonProperty("dayOfExecution")
  private DayOfExecution dayOfExecution;

  @JsonProperty("limitAmount")
  private Amount limitAmount;

  public StandingOrderDetails startDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * The first applicable day of execution starting from this date is the first payment. 
   * @return startDate
  */
  @NotNull @Valid 
  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public StandingOrderDetails frequency(FrequencyCode frequency) {
    this.frequency = frequency;
    return this;
  }

  /**
   * Get frequency
   * @return frequency
  */
  @NotNull @Valid 
  public FrequencyCode getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyCode frequency) {
    this.frequency = frequency;
  }

  public StandingOrderDetails endDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * The last applicable day of execution. If not given, it is an infinite standing order. 
   * @return endDate
  */
  @Valid 
  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public StandingOrderDetails executionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
    return this;
  }

  /**
   * Get executionRule
   * @return executionRule
  */
  @Valid 
  public ExecutionRule getExecutionRule() {
    return executionRule;
  }

  public void setExecutionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
  }

  public StandingOrderDetails withinAMonthFlag(Boolean withinAMonthFlag) {
    this.withinAMonthFlag = withinAMonthFlag;
    return this;
  }

  /**
   * This element is only used in case of frequency equals \"Monthly\".  If this element equals false it has no effect. If this element equals true, then the execution rule is overruled if the day of execution would fall into a different month using the execution rule.  Example: executionRule equals \"preceding\", dayOfExecution equals \"02\" and the second of a month is a Sunday.  In this case, the transaction date would be on the last day of the month before.  This would be overruled if withinAMonthFlag equals true and the payment is processed on Monday the third of the Month. Remark: This attribute is rarely supported in the market. 
   * @return withinAMonthFlag
  */
  
  public Boolean getWithinAMonthFlag() {
    return withinAMonthFlag;
  }

  public void setWithinAMonthFlag(Boolean withinAMonthFlag) {
    this.withinAMonthFlag = withinAMonthFlag;
  }

  public StandingOrderDetails monthsOfExecution(List<MonthsOfExecutionEnum> monthsOfExecution) {
    this.monthsOfExecution = monthsOfExecution;
    return this;
  }

  public StandingOrderDetails addMonthsOfExecutionItem(MonthsOfExecutionEnum monthsOfExecutionItem) {
    if (this.monthsOfExecution == null) {
      this.monthsOfExecution = new ArrayList<>();
    }
    this.monthsOfExecution.add(monthsOfExecutionItem);
    return this;
  }

  /**
   * The format is following the regular expression \\d{1,2}.  The array is restricted to 11 entries.  The values contained in the array entries shall all be different and the maximum value of one entry is 12. This attribute is contained if and only if the frequency equals \"MonthlyVariable\". Example: An execution on January, April and October each year is addressed by [\"1\", \"4\", \"10\"]. 
   * @return monthsOfExecution
  */
  @Size(max = 11) 
  public List<MonthsOfExecutionEnum> getMonthsOfExecution() {
    return monthsOfExecution;
  }

  public void setMonthsOfExecution(List<MonthsOfExecutionEnum> monthsOfExecution) {
    this.monthsOfExecution = monthsOfExecution;
  }

  public StandingOrderDetails multiplicator(Integer multiplicator) {
    this.multiplicator = multiplicator;
    return this;
  }

  /**
   * This is multiplying the given frequency resulting the exact frequency, e.g. Frequency=weekly and multiplicator=3 means every 3 weeks. Remark: This attribute is rarely supported in the market. 
   * @return multiplicator
  */
  
  public Integer getMultiplicator() {
    return multiplicator;
  }

  public void setMultiplicator(Integer multiplicator) {
    this.multiplicator = multiplicator;
  }

  public StandingOrderDetails dayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
    return this;
  }

  /**
   * Get dayOfExecution
   * @return dayOfExecution
  */
  @Valid 
  public DayOfExecution getDayOfExecution() {
    return dayOfExecution;
  }

  public void setDayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
  }

  public StandingOrderDetails limitAmount(Amount limitAmount) {
    this.limitAmount = limitAmount;
    return this;
  }

  /**
   * Get limitAmount
   * @return limitAmount
  */
  @Valid 
  public Amount getLimitAmount() {
    return limitAmount;
  }

  public void setLimitAmount(Amount limitAmount) {
    this.limitAmount = limitAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StandingOrderDetails standingOrderDetails = (StandingOrderDetails) o;
    return Objects.equals(this.startDate, standingOrderDetails.startDate) &&
        Objects.equals(this.frequency, standingOrderDetails.frequency) &&
        Objects.equals(this.endDate, standingOrderDetails.endDate) &&
        Objects.equals(this.executionRule, standingOrderDetails.executionRule) &&
        Objects.equals(this.withinAMonthFlag, standingOrderDetails.withinAMonthFlag) &&
        Objects.equals(this.monthsOfExecution, standingOrderDetails.monthsOfExecution) &&
        Objects.equals(this.multiplicator, standingOrderDetails.multiplicator) &&
        Objects.equals(this.dayOfExecution, standingOrderDetails.dayOfExecution) &&
        Objects.equals(this.limitAmount, standingOrderDetails.limitAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(startDate, frequency, endDate, executionRule, withinAMonthFlag, monthsOfExecution, multiplicator, dayOfExecution, limitAmount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StandingOrderDetails {\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
    sb.append("    withinAMonthFlag: ").append(toIndentedString(withinAMonthFlag)).append("\n");
    sb.append("    monthsOfExecution: ").append(toIndentedString(monthsOfExecution)).append("\n");
    sb.append("    multiplicator: ").append(toIndentedString(multiplicator)).append("\n");
    sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
    sb.append("    limitAmount: ").append(toIndentedString(limitAmount)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

