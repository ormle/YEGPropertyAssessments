package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssessmentClasses{
    private int classPer1;
    private int classPer2;
    private int classPer3;

    private String class1;
    private String class2;
    private String class3;

    public AssessmentClasses() {
        this (0,0,0,null,null,null);
    }
    public AssessmentClasses(int classPer1, String class1) {
        this (classPer1,0,0,class1,null,null);
    }
    public AssessmentClasses(int classPer1, int classPer2, String class1, String class2) {
        this (classPer1,classPer2,0,class1,class2,null);
    }
    public AssessmentClasses(int classPer1, int classPer2, int classPer3, String class1, String class2, String class3) {
        this.classPer1 = classPer1;
        this.classPer2 = classPer2;
        this.classPer3 = classPer3;
        this.class1 = class1;
        this.class2 = class2;
        this.class3 = class3;
    }

    //getters
    public int getClassPer1() {
        return classPer1;
    }
    public int getClassPer2() {
        return classPer2;
    }
    public int getClassPer3() {
        return classPer3;
    }

    public String getClass1() {
        return class1;
    }

    public String getClass2() {
        return class2;
    }

    public String getClass3() {
        return class3;
    }

    public List<String> getClassNames(){
        ArrayList<String> names = new ArrayList<>();
        if (class3 != null && !class3.isEmpty()){
            names.add(class1);
            names.add(class2);
            names.add(class3);
        } else if (class2!= null && !class2.isEmpty()) {
            names.add(class1);
            names.add(class2);
        } else if (class1 != null && !class1.isEmpty()) {
            names.add(class1);
        }
        return names;
    }

    //setters
    public void setClassPer1(int classPer1) {
        this.classPer1 = classPer1;
    }
    public void setClassPer2(int classPer2) {
        this.classPer2 = classPer2;
    }
    public void setClassPer3(int classPer3) {
        this.classPer3 = classPer3;
    }

    public void setClass1(String class1) {
        this.class1 = class1;
    }

    public void setClass2(String class2) {
        this.class2 = class2;
    }

    public void setClass3(String class3) {
        this.class3 = class3;
    }

    public boolean hasClass(String className){
        return className.equalsIgnoreCase(class1) || className.equalsIgnoreCase(class2) || className.equalsIgnoreCase(class3);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        AssessmentClasses aC = (AssessmentClasses) o;
        if (class3 != null) {//something in all classes so compare all
            return classPer1 == aC.classPer1 && classPer2 == aC.classPer2 && classPer3 == aC.classPer3 &&
                    class1.equals(aC.class1) && class2.equals(aC.class2) && class3.equals(aC.class3);
        } else if (class2 != null) {//only compare 2 classes
            return classPer1 == aC.classPer1 && classPer2 == aC.classPer2 &&
                    class1.equals(aC.class1) && class2.equals(aC.class2);
        }
        return classPer1 == aC.classPer1 && class1.equals(aC.class1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classPer1, class1, classPer2, class2, classPer3, class3);
    }

    @Override
    public String toString() {
        if (class3 != null) { //there's something in class 3 so all classes need to be printed
            return "[" + class1 + " " + classPer1 + "%, "
                    + class2 + " " + classPer2 + "%, " + class3 + " " + classPer3 + "%]";
        } else if (class2 != null) {//nothing in 3 but something in 2 so print 2 classes
            return "[" + class1 + " " + classPer1 + "%, " + class2 + " " + classPer2 + "%]";
        } else if (class1 != null) {//nothing in 2 but something in 1 so print 1 class
            return "[" + class1 + " " + classPer1 + "%]";
        } else {//all classes are null so print empty list
            return "[]";
        }
    }
}
