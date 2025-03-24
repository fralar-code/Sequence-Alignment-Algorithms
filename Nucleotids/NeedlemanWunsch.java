import java.util.*;

public class NeedlemanWunsch{

    
    public static double max(double a, double b){
        return (a>b)? a : b;
    }
    public static double max(double a, double b, double c){
        return max(a,max(b,c));
    }

    public static String blankSpaces(int length){
        StringBuilder spaces = new StringBuilder();
        for(int i=0; i<length-1; i++)
            spaces.append(" ");
        return spaces.toString();
    }

    public static void backTracking(DpCell[][] dptable, int i, int j, String A, String B, Map<Character,String> currentAlign){
        DpCell currentCell = dptable[i][j];
        //base case
        if(currentCell.baseCase()){
            //if i=0, it means that the recursive call has returned the string B fully aligned up to its initial character
            if(i==0){
                //precede A with its remaining string
                String prev_A = A.substring(0, j+1);
                currentAlign.put('A', prev_A+currentAlign.get('A'));
                //precede B by as many blanks as the remaining string of A
                String spaces = blankSpaces(prev_A.length());
                currentAlign.put('B', spaces+B.charAt(i)+currentAlign.get('B'));
            }
            //conversely if j=0, it means that the recursive call has returned the string A fully aligned up to its initial character
            else{
                //precede B with its remaining string
                String prev_B = B.substring(0, i+1);
                currentAlign.put('B', prev_B+currentAlign.get('B'));
                //precede A by as many blanks as the remaining string of B
                String spaces = blankSpaces(prev_B.length());
                currentAlign.put('A', spaces+A.charAt(j)+currentAlign.get('A'));
            }
            //print the found alignment
            System.out.println("Alignment:");
            System.out.println("A: "+currentAlign.get('A')+"\n"+"B: "+currentAlign.get('B'));
            return;
        }
              
        //If the maximum is in a, introduce a gap in string B
        if(currentCell.isFromLeft()){
            backTracking(dptable, i, j-1, A, B, new TreeMap<Character,String>(){{
                                                            put('A', A.charAt(j)+currentAlign.get('A')); 
                                                            put('B', "-"+currentAlign.get('B'));}});
        }
        //If the maximum is in b, paring
        if(currentCell.isFromDiag()){
            backTracking(dptable, i-1, j-1, A, B, new TreeMap<Character,String>(){{
                                                                put('A', A.charAt(j)+currentAlign.get('A'));
                                                                put('B', B.charAt(i)+currentAlign.get('B'));}});
        }
        //If the maximum is in c, introduce a gap in A
        if(currentCell.isFromUp()){
            backTracking(dptable, i-1, j, A, B, new TreeMap<Character,String>(){{
                                                            put('A', "-"+currentAlign.get('A') );
                                                            put('B', B.charAt(i)+currentAlign.get('B') );}});
        }
        return;
    }

    public static void fillDp(DpCell[][] dptable, String A, String B, double delta){
        int n = B.length();
        int m = A.length();

        double s_ij, a, b, c, max;
        //initial element of dptable
        if(A.charAt(0) == B.charAt(0)){
            dptable[0][0].setValue(1);
        }else{
            dptable[0][0].setValue(0);
        }
        dptable[0][0].setMaxCode((byte)0);
        //first row
        for(int j = 1; j < m; j++){
            s_ij = (A.charAt(j) == B.charAt(0)) ? 1 : 0;
            a = dptable[0][j-1].getValue()-delta;
            dptable[0][j].setValue(max(s_ij, a));
            //If the maximum comes from the left, encode it as 100; otherwise, if it is equal to itself, encode as 0
            dptable[0][j].setMaxCode((a>=s_ij)? 4 : (byte)0);

        }
        //first column
        for(int i = 1; i < n; i++){
            s_ij = (A.charAt(0) == B.charAt(i)) ? 1 : 0;
            c = dptable[i-1][0].getValue()-delta;
            dptable[i][0].setValue(max(s_ij, c));
            //If the maximum comes from above (comes form c) encode it as 001
            dptable[i][0].setMaxCode((c>=s_ij)? 1 : (byte)0);
        }
        //Filling using the recursive formula
        for(int i = 1; i < n; i++){
            for(int j = 1; j < m; j++){
                s_ij = (A.charAt(j) == B.charAt(i)) ? 1 : 0;
                a = dptable[i][j-1].getValue()-delta;
                b = dptable[i-1][j-1].getValue()+s_ij;
                c = dptable[i-1][j].getValue()-delta;
                max = max(a, b, c);
                dptable[i][j].setValue(max);
                dptable[i][j].setMaxCode(DpCell.encode(a, b, c, max));
            }
        }
    }

    public static void main(String[] args) {
        String A = "GCATGCG";
        String B = "GATTACA";
        //if strings are not defined on the nucleotide alphabet exit from the program
        if(!A.matches("[ATGC]*") || !B.matches("[ATGC]*")){
            System.out.println("Error in string characters");
            return;
        }
        double delta = 0.5;
        DpTable dptable= new DpTable(A, B);
        fillDp(dptable.getMatrix(), A, B, delta);
        
        ArrayList<Coord> max_indexes = dptable.getMaxLastRowColomn();
        for(Coord max_index : max_indexes){
            backTracking(dptable.getMatrix(), max_index.getX(), max_index.getY(), A, B, new TreeMap<Character,String>(){{
                                                                                put('A', A.substring(max_index.getY()+1 )); 
                                                                                put('B', B.substring(max_index.getX()+1 ));}} );
        }
        }
}



class DpCell{
        double value;
        byte maxCode;//maxCode encode the byte 00000abc

        public double getValue(){
        return this.value;
        }
        public int getMaxCode(){
            return this.maxCode;
        }
        public void setValue(double value){
            this.value=value;
        }
        public void setMaxCode(byte maxCode){
            this.maxCode=maxCode;
        }

        // a --> (i,j-1)
        // b --> (i-1,j-1)
        // c --> (i-1,j)
        //b c
        //a
        public static byte encode(double a, double b, double c, double max){
            if(max==a){
                if(max==b){
                    if(max==c)
                        return 7; //111 
                    return 6; //110
                }
                if(max==c)
                    return 5; //101
                return 4; //100
            }
            if(max==b){
                if(max==c)
                    return 3; //011
                return 2; //010
            }
            return 1; //001
        }

        //Coming from the left is represented by bit a in sequence abc
        //From 100 to 111, the maximum comes from the left
        public Boolean isFromLeft(){
            return (this.maxCode>=4)? true : false;
        }
        //The diagonal is represented by bit b in sequence abc
        //Values 010, 011, 110, and 111 indicate b is set
        public Boolean isFromDiag(){
            if(this.maxCode==2 || this.maxCode==3 || this.maxCode==6 || this.maxCode==7)
                return true;
            return false;
        }
        //The above value is represented by bit c in sequence abc
        //Corresponds to values that are multiples of 3 (and thus not divisible by 2)
        //001, 011, 101, 111 
        public Boolean isFromUp(){
            if((this.maxCode%2)!=0) 
                return true;
            return false;
        }

        public Boolean baseCase(){
            return (this.maxCode == 0)? true : false;
        }
    }

class DpTable{

    private DpCell[][] matrix;
    private String A;
    private String B;


    public DpTable(String A, String B){
        int n = B.length();
        int m = A.length();
        DpCell [][] matrix = new DpCell[n][m];
        for (int i=0; i < n; i++){
            for (int j=0; j <m; j++){
                matrix[i][j] = new DpCell();
            }
        }
        this.matrix = matrix;
        this.A = A;
        this.B = B;
    }

    public DpCell[][] getMatrix(){
        return this.matrix;
    }



    public void printDpValue(){
        int n = this.B.length();
        int m = this.A.length();
        for (int i=0; i < n; i++){
            for (int j=0; j < m; j++){
                //System.out.print(this.matrix[i][j].getValue()+" ");
                System.out.printf("%1.1f  ",this.matrix[i][j].getValue());
            }
            System.out.println();
        }
    }

    public void printDpMax(){
        int n = this.B.length();
        int m = this.A.length();
        for (int i=0; i < n; i++){
            for (int j=0; j < m; j++){
                System.out.printf("%4d", this.matrix[i][j].getMaxCode());
            }
            System.out.println();
        }
    }

    //Returns the positions of the maximum values found in the last row and last column of the matrix
    public ArrayList<Coord> getMaxLastRowColomn(){
        int n = this.B.length();
        int m = this.A.length();
        DpCell [][] dptable = this.matrix;
        ArrayList<Coord> max_indexes = new ArrayList<Coord>();
        max_indexes.add(new Coord(n-1, 0));
        
        //Search for maxima in the last row (x is fixed to n-1)
        for (int j=1; j<m; j++){
            //If the current element is greater than the currently found maximum, reallocate the array of maximum indices 
            if(dptable[n-1][j].getValue() > dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                max_indexes = new ArrayList<Coord>();
                max_indexes.add(new Coord(n-1, j));
            }else if(dptable[n-1][j].getValue() == dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                max_indexes.add(new Coord(n-1, j));
            }
        }
        //Search for maxima in the last column (y is fixed to m-1)
        for (int i=0; i<n-1; i++){
            if(dptable[i][m-1].getValue() > dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                max_indexes = new ArrayList<Coord>();
                max_indexes.add(new Coord(i, m-1));
            }else if(dptable[i][m-1].getValue() == dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                max_indexes.add(new Coord(i, m-1));
            }
        }
        return max_indexes;
    }
}

class Coord{
    private int x;
    private int y;

    public Coord(){};
    public Coord(int x, int y){
        this.x = x;
        this.y = y;
    }
    public int getX(){
        return this.x;
    }
    public int getY(){
        return this.y;
    }
}