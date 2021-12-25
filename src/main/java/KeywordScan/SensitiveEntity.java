package KeywordScan;

import java.io.Serializable;

public class SensitiveEntity implements Serializable {

    private String rulename;
    private String rules ;
    private String node;

    public SensitiveEntity() {
    }


    public SensitiveEntity(String rulename, String rules, String node) {
        this.rulename = rulename;
        this.rules = rules;
        this.node = node;
    }

    public String getRulename() {
        return rulename;
    }

    public void setRulename(String rulename) {
        this.rulename = rulename;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "sensitiveEntity2{" +
                "rulename='" + rulename + '\'' +
                ", rules='" + rules + '\'' +
                ", nod='" + node + '\'' +
                '}';
    }
}
