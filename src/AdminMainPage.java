import java.util.*;
import java.sql.*;

public class AdminMainPage {
    static PreparedStatement pstmt= null;
    static ResultSet rs = null;
    static ResultSetMetaData rsmd = null;
    static int showReportList(){
        try{
//            String query = "select report_id,u_id,it_id from report where admin_id=?";
//            pstmt = Main.conn.prepareStatement(query);
//            pstmt.setString(1, Main.userid);
            String query = "select report_id,u_id,it_id from report";
            pstmt = Main.conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            rsmd = rs.getMetaData();
            int cnt = rsmd.getColumnCount();
            System.out.print("order\t");
            for(int i=1; i<=cnt; i++){
                System.out.print(rsmd.getColumnName(i)+"\t");
            }
            System.out.println("\n-------------------------------------");
            while(rs.next()){
                System.out.print(rs.getInt(1)+"\t"+rs.getString(2)+"\t"
                        +rs.getInt(3));
                System.out.println();
            }
            rs.close();
            pstmt.close();
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
        return 0;
    }
    static int showUserList(){
        int command;
        int order = 1;
        Scanner scanner = new Scanner(System.in);
        while(true){
            //회원 목록 출력하는 부분
            try{
                String query = "select u_id,name,tel,email from member";
                pstmt = Main.conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                출처: https://endorphin0710.tistory.com/76 [Festina Lente]
                rs = pstmt.executeQuery();
                rsmd = rs.getMetaData();
                int cnt = rsmd.getColumnCount();
                System.out.println();
                for(int i=1; i<=cnt; i++){
                    System.out.print(rsmd.getColumnName(i)+"\t");
                }
                System.out.println("\n-------------------------------------");
                while(rs.next()){
                    System.out.printf("%4d) %12s %20s %12s %20s",order++, rs.getString(1),rs.getString(2),
                            rs.getString(3),rs.getString(4));
                    System.out.println();
                }
                order = 1;
            }catch (SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
            //커멘드 입력
            System.out.println("0)뒤로가기 order)해당 회원 상세페이지");
            command = scanner.nextInt();
            if(command == 0)
                return 0;
            else{
                //입력 받은 순번의 user id 받음
                try{
                    rs.first();
                    for(int i = 0; i < command-1 ; i++)
                        rs.next();
                    memberDetail(rs.getString(1));
                }catch (SQLException sqlException){
                    System.out.println(sqlException.getMessage());
                }
            }
        }
    }
    static int memberDetail(String u_id){
        int command;
        String query;
        //회원 상세 정보 확인
        try{
            query = "select * from member where u_id=?";
            pstmt = Main.conn.prepareStatement(query);
            pstmt.setString(1,u_id);
            rs = pstmt.executeQuery();
            rs.next();
            System.out.println("--------------------------------------");
            System.out.printf("| User ID     : %20s |\n",rs.getString(1));
            System.out.printf("| User PW     : %20s |\n",rs.getString(2));
            System.out.printf("| NAME        : %20s |\n",rs.getString(3));
            System.out.printf("| TEL         : %20s |\n",rs.getString(6));
            System.out.printf("| EMAIL       : %20s |\n",rs.getString(7));
            System.out.printf("| AVG Score   : %20s |\n",rs.getString(5));
            System.out.println("--------------------------------------");
            System.out.printf("User Description : %s\n\n",rs.getString(4));
            rs.close();
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("1)회원삭제 2)뒤로가기");
        command = scanner.nextInt();
        switch (command){
            case 1:
                //오류 발생합니당.
                try{
                    query = "delete from member where u_id=?";
                    pstmt = Main.conn.prepareStatement(query);
                    pstmt.setString(1, u_id);
                    System.out.println(pstmt.executeUpdate());
                }catch (SQLException sqlException){
                    System.out.println(sqlException.getMessage());
                }
                System.out.println("회원 삭제 완료");
                return 0;
            case 2:
                return 0;
        }
        return 0;
    }
    static int addAdmin(){
        return 0;
    }
}
