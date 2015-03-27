package com.ctrip.infosec.flowtable4j.bwlist;
import com.ctrip.infosec.flowtable4j.model.BWFact;
import com.ctrip.infosec.flowtable4j.model.CheckType;
import com.ctrip.infosec.flowtable4j.model.RiskResult;

import java.util.Date;
import java.util.List;

/**
 * Created by thyang on 2015/3/13 0013.
 */
public class RuleStatement {
    private Integer ruleID;
    private Integer riskLevel;
    private String remark;
    private Date effectDate;
    private Date expireDate;
    private List<RuleTerm> ruleTerms;
    private Integer orderType;
    private String ruleIDName;

    public String getRuleIDName() {
        return ruleIDName;
    }

    public void setRuleIDName(String ruleIDName) {
        this.ruleIDName = ruleIDName;
    }

    public RuleTerm getEQRuleTerm() {
        if (ruleTerms != null && ruleTerms.size() > 0) {
            for (RuleTerm term : ruleTerms) {
                if ("EQ".equals(term.getOperator())) {
                    return term;
                }
            }
        }
        return null;
    }

    public RuleTerm getFirstRuleTerm() {
        if (ruleTerms != null && ruleTerms.size() > 0) {
            return ruleTerms.get(0);
        }
        return null;
    }

    public Integer getRuleID() {
        return ruleID;
    }

    public void setRuleID(Integer ruleID) {
        this.ruleID = ruleID;
    }

    public Integer getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(Integer riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(Date effectDate) {
        this.effectDate = effectDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public List<RuleTerm> getRuleTerms() {
        return ruleTerms;
    }

    public void setRuleTerms(List<RuleTerm> ruleTerms) {
        this.ruleTerms = ruleTerms;
    }

    public boolean check(BWFact fact, List<RiskResult> results) {
        Date now = new Date();
        boolean match = false;

        if (now.compareTo(effectDate) >= 0 && now.compareTo(expireDate) < 0) {
            match = true;
            if (ruleTerms != null && ruleTerms.size() > 0) {
                for (RuleTerm term : ruleTerms) {
                    if (!term.check(fact)) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                RiskResult result = new RiskResult();
                result.setRuleID(ruleID);
                result.setRiskLevel(riskLevel);
                result.setRuleRemark(remark);
                result.setRuleName(ruleIDName);
                result.setRuleType(CheckType.BW.toString());
                results.add(result);
            }
        }
        return match;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RuleStatement) {
            RuleStatement rs = (RuleStatement) obj;
            return this.ruleID.equals(rs.getRuleID());
        }
        return super.equals(obj);
    }

}

