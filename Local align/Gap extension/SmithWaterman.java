import java.util.*;

public class SmithWaterman{

    
    public static double max(double a, double b){
        return (a>b)? a : b;
    }
    public static double max(double a, double b, double c){
        return max(a,max(b,c));
    }
    public static double max(double a, double b, double c, double d){
        return max(a,max(b,max(c,d)));
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
                String prev_A= A.substring(0, j+1);
                currentAlign.put('A',prev_A+"|"+currentAlign.get('A'));
                //precede B by blankspaces to print the right local alignment
                String spaces = blankSpaces(prev_A.length());
                currentAlign.put('B',spaces+B.charAt(i)+"|"+currentAlign.get('B'));
            }
            //conversely if j=0, it means that the recursive call has returned the string A fully aligned up to its initial character
            else{
                //precede B with its remaining string
                String prev_B = B.substring(0, i+1);
                currentAlign.put('B', prev_B+"|"+currentAlign.get('B'));
                //precede A by blankspaces to print the right local alignment
                String spaces = blankSpaces(prev_B.length());
                currentAlign.put('A', spaces+A.charAt(j)+"|"+currentAlign.get('A'));
            }
            //print the found alignment
            System.out.println("Alignment:");
            System.out.println("A: "+currentAlign.get('A')+"\n"+"B: "+currentAlign.get('B'));
            return;
        }

        //If during backtracking we came from below, we need to check whether we are constrained to follow the extension gap
        if(currentCell.isFromUnder()){
            // If the maximum of the current cell comes from above, it means it considered the gap choice in A
            // If the maximum of the above cell comes from its upper cell, then the considered gap is an extension gap
            // We are constrained to follow the alignment that continues with the extension gap
            if(currentCell.isFromUp() && i>1 && dptable[i-1][j].isFromUp()){
                dptable[i-1][j].setFromUnder();
                backTracking(dptable, i-1, j, A, B, new TreeMap<Character,String>(){{
                                                            put('A', "-"+currentAlign.get('A') );
                                                            put('B', B.charAt(i)+currentAlign.get('B') );}});
                return;
            }
        }
        //If during backtracking we came from the right, we need to check whether we are constrained to follow the extension gap
        if(currentCell.isFromRight()){
            // If the maximum of the current cell comes from the left, it means it considered the gap choice in B
            // If the maximum of the left cell comes from its leftmost cell, then the considered gap is an extension gap
            // We are constrained to follow the alignment that continues with the extension gap
            if(currentCell.isFromLeft() && j>1 && dptable[i][j-1].isFromLeft()){
                dptable[i][j-1].setFromRight();
                backTracking(dptable, i, j-1, A, B, new TreeMap<Character,String>(){{
                                                            put('A', A.charAt(j)+currentAlign.get('A')); 
                                                            put('B', "-"+currentAlign.get('B'));}});
                return;
            }
        }
              
        //If the maximum is in a, introduce a gap in string B
        if(currentCell.isFromLeft()){
            dptable[i][j-1].setFromRight();
            backTracking(dptable, i, j-1, A, B, new TreeMap<Character,String>(){{
                                                            put('A', A.charAt(j)+currentAlign.get('A')); 
                                                            put('B', "-"+currentAlign.get('B'));}});
        }
        //If the maximum is in b, paring
        if(currentCell.isFromDiag()){
            dptable[i-1][j-1].setFromDiag();
            backTracking(dptable, i-1, j-1, A, B, new TreeMap<Character,String>(){{
                                                                put('A', A.charAt(j)+currentAlign.get('A'));
                                                                put('B', B.charAt(i)+currentAlign.get('B'));}});
        }
        //If the maximum is in c, introduce a gap in A
        if(currentCell.isFromUp()){
            dptable[i-1][j].setFromUnder();
            backTracking(dptable, i-1, j, A, B, new TreeMap<Character,String>(){{
                                                            put('A', "-"+currentAlign.get('A') );
                                                            put('B', B.charAt(i)+currentAlign.get('B') );}});
        }
        return;
    }

    public static ArrayList<Coord> fillDp(DpCell[][] dptable, String A, String B, double delta, double gamma, double T){
        //in the case of local alignment the matrix does not need to be normalized, 
        //in the filling law we calculate the max also considering the zero valure i.e. the beginning of the local alignment, 
        // we go to consider the extended matrix i.e. the matrix with first row and column null
        Blosum blosum = new Blosum();
        int n = B.length()+1;
        int m = A.length()+1;
        //first column of zeros
        for (int i = 0; i < n; i++){
            dptable[i][0].setValue(0);
        }//first row of zeros
        for (int j = 0; j < m; j++){
            dptable[0][j].setValue(0);
        }

        ArrayList<Coord> max_indexes = new ArrayList<Coord>(); 
        double s_ij, a, b, c, max;
        //initial element of dptable
        s_ij = blosum.matrix[blosum.getIndex(A.charAt(0))][blosum.getIndex(B.charAt(0))];
        dptable[1][1].setValue(max(0, s_ij));
        dptable[1][1].setMaxCode((byte)2);
        if (dptable[1][1].getValue()>=T)
            max_indexes.add(new Coord(1,1));
        //first row
        for(int j = 2; j < m; j++){
            s_ij = blosum.matrix[blosum.getIndex(B.charAt(0))][blosum.getIndex(A.charAt(j-1))];
            //If the preceding element comes from the left, consider the gap extension penalty; otherwise, gap open
            if (dptable[1][j-1].isFromLeft())
                a = dptable[1][j-1].getValue()-gamma;
            else
                a = dptable[1][j-1].getValue()-delta;
            dptable[1][j].setValue(max(s_ij, a, 0));
            //If the maximum comes from the left, encode it as 100; otherwise, if it is equal to itself, it comes from the diag, encode as 0
            dptable[1][j].setMaxCode((a>=s_ij)? 4 : (byte)2);
            if (dptable[1][j].getValue()>=T)
                max_indexes.add(new Coord(1,j));
        }
        
        //first column
        for(int i = 2; i < n; i++){
            s_ij = blosum.matrix[blosum.getIndex(B.charAt(i-1))][blosum.getIndex(A.charAt(0))];
            // If the preceding element comes from above (comes from a) consider the gap extension penalty; otherwise, gap open
            if(dptable[i-1][1].isFromUp())
                c = dptable[i-1][1].getValue()-gamma;
            else
                c = dptable[i-1][1].getValue()-delta;
            dptable[i][1].setValue(max(s_ij, c, 0));
            //If the maximum comes from above (comes form c) encode it as 001
            dptable[i][1].setMaxCode((c>=s_ij)? 1 : (byte)2);
            if (dptable[i][1].getValue()>=T)
                max_indexes.add(new Coord(i,1));
        }
        //Filling using the recursive formula
        for(int i = 2; i < n; i++){
            for(int j = 2; j < m; j++){
                s_ij = blosum.matrix[blosum.getIndex(B.charAt(i-1))][blosum.getIndex(A.charAt(j-1))];
                b = dptable[i-1][j-1].getValue()+s_ij;
                //If the maximum of a comes from the left, consider gamma (gap extension)
                if (dptable[i][j-1].isFromLeft())
                    a = dptable[i][j-1].getValue()-gamma;
                else
                    a = dptable[i][j-1].getValue()-delta;
                //If the maximum of c comes from above, consider gamma (gap extension)
                if (dptable[i-1][j].isFromUp())
                    c = dptable[i-1][j].getValue()-gamma;
                else
                    c = dptable[i-1][j].getValue()-delta;
                max = max(a, b, c, 0);
                dptable[i][j].setValue(max);
                dptable[i][j].setMaxCode(DpCell.encode(a, b, c, max));
                if (dptable[i][j].getValue()>=T)
                    max_indexes.add(new Coord(i,j));
            }
        }
        return max_indexes;
    }

    public static void main(String[] args) {

        String A = "LTGARDWEDIPLWTDWDIEQESDFKTRAFGTANCHK";
        String B = "ATGIPLWTDWDLEQESDNSCNTDHYTREWGTMNAHKAG";
        //String A = "TFDERILGVQTYWAECLA";
        //String B = "QTFWECIKGDNATY";
        double delta = 5.0;
        double gamma = 1.0;
        double T = 31;

        //expanded matrix with one column at zero and one row at zero
        DpTable dptable= new DpTable(A, B);
        ArrayList<Coord> max_indexes = fillDp(dptable.getMatrix(), A, B, delta, gamma, T);
        dptable.printDpValue();
        max_indexes = dptable.getMax();
        
        for(Coord max_index : max_indexes){
            System.out.println("SCORE: "+dptable.getMatrix()[max_index.getX()][max_index.getY()].getValue());
            backTracking(dptable.getMatrix(), max_index.getX(), max_index.getY(), " "+A, " "+B, new TreeMap<Character,String>(){{
                                                                                put('A', "|"+A.substring(max_index.getY())); 
                                                                                put('B', "|"+B.substring(max_index.getX()));}} );
        }
        }
}



class DpCell{
        double value;
        byte maxCode;//maxCode encode the byte 000urabc

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
            if(max==0)
                return 0; //the maximum is neither a,b,c but is the zero value
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

        //The current maximum encoding is the byte urabc, which represents this scenario for any cell x
        // b c
        // a x r
        //   u
        //abc are involved during table filling (indicating where the maximum comes from)
        //ur are involved during backtracking, indicating from which cell the recursive call originated

        //maxCode encode the byte 000urabc
        //Sets bit u to 1, i.e., performs OR between 1000 and the current maxCode
        public void setFromUnder(){
            this.setFromDiag();
            byte set= (byte)1<<4;
            this.maxCode=(byte)( set | this.maxCode); 
        }
        //Sets bit r to 1, i.e., performs OR between 100 and the current maxCode
        public void setFromRight(){
            this.setFromDiag();
            byte set= (byte)1<<3;
            this.maxCode=(byte)( set | this.maxCode); 
        }

        //If coming from the diagonal, clear bits u and r
        public void setFromDiag(){
            //This is a bit-clear operation: the sequence 11000 negated is ANDed with maxCode
            byte set= (byte)3<<3;
            this.maxCode=(byte)( (~set) & this.maxCode); 
        }

        //Coming from the left is represented by bit a in sequence abc
        //From 100 to 111, the maximum comes from the left
        public Boolean isFromLeft(){
            byte set= (byte)3<<3;
            int abc_Code=(byte)( (~set) & this.maxCode);
            return (abc_Code>=4)? true : false;
        }
        //The diagonal is represented by bit b in sequence abc
        //Values 010, 011, 110, and 111 indicate b is set
        public Boolean isFromDiag(){
            byte set= (byte)3<<3;
            int abc_Code=(byte)( (~set) & this.maxCode);
            if(abc_Code==2 || abc_Code==3 || abc_Code==6 || abc_Code==7)
                return true;
            return false;
        }
        //The above value is represented by bit c in sequence abc
        //Corresponds to values that are multiples of 3 (and thus not divisible by 2)
        //001, 011, 101, 111 
        public Boolean isFromUp(){
            byte set= (byte)3<<3;
            int control=(byte)( (~set) & this.maxCode);
            if((control%2)!=0) 
                return true;
            return false;
        }

        public Boolean baseCase(){
            return (this.value == 0)? true : false;
        }

        ///urabc
        //Need to check if bit u is set, i.e., shift maxCode right by 4
        public Boolean isFromUnder(){
            return ((byte)this.maxCode>>4)==1? true : false;
        }
        //urabc
        //Need to check if bit r is set
        public Boolean isFromRight(){
            byte set= (byte)1<<3;
            return (((set & this.maxCode)>>3)==1) ? true : false;
        }
    }

class DpTable{

    private DpCell[][] matrix;
    private String A;
    private String B;


    public DpTable(String A, String B){
        int n = B.length()+1;
        int m = A.length()+1;
        DpCell [][] matrix = new DpCell[n][m];
        for (int i=0; i < n; i++){
            for (int j=0; j <m; j++){
                matrix[i][j] = new DpCell();
            }
        }
        this.matrix = matrix;
        this.A = " "+A;
        this.B = " "+B;
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
                System.out.printf("%4d",(int) this.matrix[i][j].getValue());
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

    public ArrayList<Coord> getMax(){
        int n = this.B.length();
        int m = this.A.length();
        DpCell [][] dptable = this.matrix;
        ArrayList<Coord> max_indexes = new ArrayList<Coord>();
        max_indexes.add(new Coord(1, 1));
        for(int i=1; i<n; i++){
            for(int j=1; j<m; j++){
                if(dptable[i][j].getValue() > dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                    max_indexes = new ArrayList<Coord>();
                    max_indexes.add(new Coord(i, j));
                }else if(dptable[i][j].getValue() == dptable[max_indexes.get(0).getX()][max_indexes.get(0).getY()].getValue()){
                    max_indexes.add(new Coord(i, j));
                }
            }
        }
        return max_indexes;
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


/**
 * Blosum62 substitution matrix
 * BLOSUM Clustered Scoring Matrix in 1/2 Bit Units
 * Blocks Database = /data/blocks_5.0/blocks.dat
 * Cluster Percentage: >= 62
 * Entropy =   0.6979, Expected =  -0.5209
 * author Antonio J. Nebro <antonio@lcc.uma.es>
 **/
class Blosum{

    public static final int[][] matrix = new int[][] {
        //A   R   N   D   C   Q   E   G   H   I   L   K   M   F   P   S   T   W   Y   V  
        //0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17  18  19  
/* A */ { 4, -1, -2, -2,  0, -1, -1,  0, -2, -1, -1, -1, -1, -2, -1,  1,  0, -3, -2,  0},
/* R */ {-1,  5,  0, -2, -3,  1,  0, -2,  0, -3, -2,  2, -1, -3, -2, -1, -1, -3, -2, -3},
/* N */ {-2,  0,  6,  1, -3,  0,  0,  0,  1, -3, -3,  0, -2, -3, -2,  1,  0, -4, -2, -3},
/* D */ {-2, -2,  1,  6, -3,  0,  2, -1, -1, -3, -4, -1, -3, -3, -1,  0, -1, -4, -3, -3},
/* C */ { 0, -3, -3, -3,  9, -3, -4, -3, -3, -1, -1, -3, -1, -2, -3, -1, -1, -2, -2, -1},
/* Q */ {-1,  1,  0,  0, -3,  5,  2, -2,  0, -3, -2,  1,  0, -3, -1,  0, -1, -2, -1, -2},
/* E */ {-1,  0,  0,  2, -4,  2,  5, -2,  0, -3, -3,  1, -2, -3, -1,  0, -1, -3, -2, -2},
/* G */ { 0, -2,  0, -1, -3, -2, -2,  6, -2, -4, -4, -2, -3, -3, -2,  0, -2, -2, -3, -3},
/* H */ {-2,  0,  1, -1, -3,  0,  0, -2,  8, -3, -3, -1, -2, -1, -2, -1, -2, -2,  2, -3},
/* I */ {-1, -3, -3, -3, -1, -3, -3, -4, -3,  4,  2, -3,  1,  0, -3, -2, -1, -3, -1,  3},
/* L */ {-1, -2, -3, -4, -1, -2, -3, -4, -3,  2,  4, -2,  2,  0, -3, -2, -1, -2, -1,  1},
/* K */ {-1,  2,  0, -1, -3,  1,  1, -2, -1, -3, -2,  5, -1, -3, -1,  0, -1, -3, -2, -2},
/* M */ {-1, -1, -2, -3, -1,  0, -2, -3, -2,  1,  2, -1,  5,  0, -2, -1, -1, -1, -1,  1},
/* F */ {-2, -3, -3, -3, -2, -3, -3, -3, -1,  0,  0, -3,  0,  6, -4, -2, -2,  1,  3, -1},
/* P */ {-1, -2, -2, -1, -3, -1, -1, -2, -2, -3, -3, -1, -2, -4,  7, -1, -1, -4, -3, -2},
/* S */ { 1, -1,  1,  0, -1,  0,  0,  0, -1, -2, -2,  0, -1, -2, -1,  4,  1, -3, -2, -2},
/* T */ { 0, -1,  0, -1, -1, -1, -1, -2, -2, -1, -1, -1, -1, -2, -1,  1,  5, -2, -2,  0},
/* W */ {-3, -3, -4, -4, -2, -2, -3, -2, -2, -3, -2, -3, -1,  1, -4, -3, -2, 11,  2, -3},
/* Y */ {-2, -2, -2, -3, -2, -1, -2, -3,  2, -1, -1, -2, -1,  3, -3, -2, -2,  2,  7, -1},
/* V */ { 0, -3, -3, -3, -1, -2, -2, -3, -3,  3,  1, -2,  1, -1, -2, -2,  0, -3, -1,  4}};


        private int getMin(){
            int min=matrix[0][0];
            for (int i=0; i < matrix.length; i++){
                for (int j=0; j <matrix[i].length; j++){
                    if (matrix[i][j] < min)
                        min = matrix[i][j];
                }
            }
            return min;
        }
        public int[][] normalized(){
            int min = this.getMin();
            int[][] norm = new int[matrix.length][matrix.length];
            for (int i=0; i < matrix.length; i++){
                for (int j=0; j <matrix[i].length; j++){
                    norm[i][j]=matrix[i][j]-min;
                }
            }
            return norm;
        }

        public int getIndex(char c) {
            switch (c) {
                case 'A': return 0 ;
                case 'R': return 1 ;
                case 'N': return 2 ;
                case 'D': return 3 ;
                case 'C': return 4 ;
                case 'Q': return 5 ;
                case 'E': return 6 ;
                case 'G': return 7 ;
                case 'H': return 8 ;
                case 'I': return 9 ;
                case 'L': return 10;
                case 'K': return 11;
                case 'M': return 12;
                case 'F': return 13;
                case 'P': return 14;
                case 'S': return 15;
                case 'T': return 16;
                case 'W': return 17;
                case 'Y': return 18;
                case 'V': return 19;
                default: throw new IllegalArgumentException("Character "+c+" not recognized by this alphabet") ;
                }
                }
}