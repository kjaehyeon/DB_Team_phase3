import java.util.*;
import java.sql.*;

public class AdminMainPage {
    static PreparedStatement pstmt= null;
    static ResultSet rs = null;
    static ResultSetMetaData rsmd = null;

    static int showReportList(){
        int order = 1;
        int command;
        Scanner scanner = new Scanner(System.in);
        //신고 목록 출력
        while(true){
            try{
                String query = "select r.report_id, i.it_id,m.u_id, m.name, r.admin_id,i.name\n" +
                        "from report r, item i, member m\n" +
                        "where r.it_id = i.it_id\n" +
                        "and r.u_id=m.u_id";
                pstmt = Main.conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = pstmt.executeQuery();

                System.out.println("      Report_ID  Item_ID    Reporter_ID  Reporter_Name        ADMIN           Item_Name");
                System.out.println("---------------------------------------------------------------------------------------------");
                while(rs.next()){
                    System.out.printf("%4d) %-10d %-10d %-12s %-20s %-15s %s\n",
                            order++, rs.getInt(1),rs.getInt(2), rs.getString(3),
                            rs.getString(4), rs.getString(5),rs.getString(6));
                }
            }catch (SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
            System.out.println("0)뒤로가기 order)해당 신고 상세페이지");
            command = scanner.nextInt();
            if(command == 0)
                return 0;
            else if(command <= order){
                order = 1;
                //입력 받은 순번의 Item id 받음
                //해당 신고의 담당자가 아니면 상세 내용 접근 불가
                try{
                    rs.first();
                    for(int i = 0; i < command-1 ; i++)
                        rs.next();
                    reportDetail(rs.getInt(1), rs.getInt(2), rs.getString(5));
                }catch (SQLException sqlException){
                    System.out.println(sqlException.getMessage());
                }
            }else{
                order = 1;
                System.out.println("Invalid command");
                continue;
            }
        }
    }
    static int showUserList(){
        int command;
        int order = 1;
        Scanner scanner = new Scanner(System.in);
        while(true){
            //회원 목록 출력하는 부분
            try{
                String query = "select m.u_id, m.name, m.tel, m.email, NVL(rc.report_count, 0) as re_count" +
                        " from member m LEFT JOIN (select i.u_id, count(*) as report_count" +
                        "                from report r, item i" +
                        "                where r.it_id = i.it_id" +
                        "                GROUP BY i.u_id) rc ON m.u_id = rc.u_id" +
                        " ORDER BY re_count DESC";
                pstmt = Main.conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                rs = pstmt.executeQuery();
                rsmd = rs.getMetaData();
                int cnt = rsmd.getColumnCount();
                System.out.println();
                System.out.print("index ");
                for(int i=1; i<=cnt; i++){
                    System.out.print(rsmd.getColumnName(i)+"\t\t\t\t");
                }
                System.out.println("\n----------------------------------------------------------------------------------------------------------");
                while(rs.next()){
                    System.out.printf("%4d) %-12s %-20s %-12s %-20s %10d",order++, rs.getString(1),rs.getString(2),
                            rs.getString(3),rs.getString(4), rs.getInt(5));
                    System.out.println();
                }
                System.out.println("----------------------------------------------------------------------------------------------------------");

            }catch (SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
            //커멘드 입력
            System.out.println("0)뒤로가기 order)해당 회원 상세페이지");
            command = scanner.nextInt();
            if(command == 0)
                return 0;
            else if(command <= order){
                order = 1;
                //입력 받은 순번의 user id 받음
                try{
                    rs.first();
                    for(int i = 0; i < command-1 ; i++)
                        rs.next();
                    memberDetail(rs.getString(1));
                }catch (SQLException sqlException){
                    System.out.println(sqlException.getMessage());
                }
            }else{
                order = 1;
                System.out.println("Invalid command");
            }
        }
    }
    static int memberDetail(String u_id){
        int command;
        String query;
        //회원 상세 정보 확인
        try{
            query = "select * " +
                    " from member " +
                    " where u_id=?";
            pstmt = Main.conn.prepareStatement(query);
            pstmt.setString(1,u_id);
            rs = pstmt.executeQuery();
            rs.next();
            System.out.println("User Information");
            System.out.println("---------------------------------------");
            System.out.printf("User ID     : %20s\n",rs.getString(1));
            System.out.printf("User PW     : %20s\n",rs.getString(2));
            System.out.printf("NAME        : %20s\n",rs.getString(3));
            System.out.printf("TEL         : %20s\n",rs.getString(6));
            System.out.printf("EMAIL       : %20s\n",rs.getString(7));
            System.out.printf("AVG Score   : %20s\n",rs.getString(5));
            System.out.printf("User Description : %s\n",rs.getString(4));
            System.out.print("Address     : ");
            query = "SELECT A.Name" +
                    " FROM ADDRESS A, LIVES_IN L" +
                    " WHERE A.Ad_id = L.Ad_id  AND L.U_id = ?";
            pstmt = Main.conn.prepareStatement(query);
            pstmt.setString(1, u_id);
            rs = pstmt.executeQuery();
            while(rs.next()) {
                System.out.printf("%s | ", rs.getString(1));
            }
            System.out.println("\n--------------------------------------");
            rs.close();
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        while (true){
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
                        query = "UPDATE ITEM SET Current_price = (" +
                                "    select MAX(i.current_priceprice)" +
                                "    from item i" +
                                "    where u_id = ?)";
                        pstmt = Main.conn.prepareStatement(query);
                        pstmt.setString(1, u_id);
                        System.out.println(pstmt.executeUpdate());
                        System.out.println("회원 삭제 완료");
                    }catch (SQLException sqlException){
                        System.out.println(sqlException.getMessage());
                        System.out.println("회원 삭제 실패");
                    }
                    return 0;
                case 2:
                    return 0;
                default:
                    System.out.println("Invalid command");
                    continue;
            }
        }
    }
    static int reportDetail(int report_id, int it_id, String admin_id){
        int command;
        String query;
        //신고 상세 정보 확인
        try {
            query = "select r.report_id, i.it_id, i.name, m1.u_id, m1.name, m2.u_id, m2.name, r.admin_id ,r.description\n" +
                    "from report r, item i, member m1, member m2\n" +
                    "where r.it_id = i.it_id\n" +
                    "and r.u_id=m1.u_id\n" +
                    "and i.u_id=m2.u_id\n"+
                    "and r.report_id=?";
            pstmt = Main.conn.prepareStatement(query);
            pstmt.setInt(1, report_id);
            rs = pstmt.executeQuery();
            rs.next();
            System.out.println("Report Detail");
            System.out.println("------------------------------------");
            System.out.println("Report_ID     : "+rs.getInt(1));
            System.out.println("Item_ID       : "+rs.getInt(2));
            System.out.println("Item_Name     : "+rs.getString(3));
            System.out.println("Reporter_ID   : "+rs.getString(4));
            System.out.println("Reporter_Name : "+rs.getString(5));
            System.out.println("Seller_ID     : "+rs.getString(6));
            System.out.println("Seller_Name   : "+rs.getString(7));
            System.out.println("Admin_ID      : "+rs.getString(8));
            System.out.println("Report Content: "+rs.getString(9));
            System.out.println("------------------------------------");
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }
        //커멘드 입력 받음, 담당자가 아니면 게시물 삭제 불가
        while (true) {
            Scanner scanner = new Scanner(System.in);
            if(Main.userid.equals(admin_id)){
                System.out.println("1)리포트 삭제 2)신고된 게시물 삭제 3)뒤로가기");
                command = scanner.nextInt();
                switch (command){
                    case 1: //신고된 게시물 삭제 하지 않고 리포트 삭제
                        try{
                            query = "delete from report where report_id=?";
                            pstmt = Main.conn.prepareStatement(query);
                            pstmt.setInt(1, report_id);
                            System.out.println(pstmt.executeUpdate());
                            System.out.println("리포트 삭제 완료");
                        }catch (SQLException sqlException){
                            System.out.println(sqlException.getMessage());
                            System.out.println("리포트 삭제 실패");
                        }
                        return 0;
                    case 2: //신고된 게시물 삭제 후 리포트 삭제
                        try{
                            query = "delete from report where report_id=?";
                            pstmt = Main.conn.prepareStatement(query);
                            pstmt.setInt(1, report_id);
                            System.out.println(pstmt.executeUpdate());
                            UserMyPage.DeleteItem(it_id);
                            System.out.println("리포트 및 게시물 삭제 완료");
                        }catch (SQLException Exception){
                            System.out.println(Exception.getMessage());
                            System.out.println("리포트 및 게시물 삭제 실패");
                        }
                        return 0;
                    case 3:
                        return 0;
                    default:
                        System.out.println("Invalid command");
                        continue;
                }
            }else { //권한이 없는 관리자
                System.out.println("1)뒤로가기");
                command = scanner.nextInt();
                if(command==1) return 0;
                else{
                    System.out.println("Invalid command");
                    continue;
                }
            }
        }
    }
    static int addAdmin(){
            String id;
            String pw;
            String name;
            Scanner scanner = new Scanner(System.in);
            while(true){
                System.out.println("관리자로 등록할 ID, PW, Name을 입력해주세요.");
                System.out.print("ID : ");
                id = scanner.nextLine().trim();
                System.out.print("PW : ");
                pw = scanner.nextLine().trim();
                System.out.print("Name : ");
                name = scanner.nextLine().trim();
                try{
                    String query = "insert into admin values(?,?,?)";
                    pstmt = Main.conn.prepareStatement(query);
                    pstmt.setString(1,id);
                    pstmt.setString(2,pw);
                    pstmt.setString(3,name);
                    pstmt.executeUpdate();
                    System.out.println("관리자 등록 성공");
                    pstmt.close();
                    return 0;
                }catch (SQLException sqlException){
                    System.out.println(sqlException.getMessage());
                    System.out.println(sqlException.getErrorCode());
                    return 0;
                }
            }
    }
    public static void reportUserListN() {
        int order = 1;
        int command;
        Scanner sc = new Scanner(System.in);
        System.out.print("몇 회 이상 신고된 유저를 조회할까요?(ex. 3) : ");
        int count = sc.nextInt();
        String sql = "select m.u_id, m.pw, m.name, m.tel, m.email, m.average_score" +
                " from member m  " +
                " where EXISTS (select i.u_id, count(*)" +
                "                from report r, item i" +
                "                where r.it_id = i.it_id and m.u_id = i.u_id" +
                "                GROUP BY i.u_id" +
                "                HAVING count(*) >= ?)";
        while (true) {
            try {
                pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                pstmt.setInt(1, count);
                rs = pstmt.executeQuery();
                System.out.printf("%6s) %-20s %-20s %-25s %-15s %-25s %-13s",
                                "Index","User_id", "PW", "Name", "Tel", "Email", "Average_Score");
                System.out.println("\n-----------------------------------------------------------------------------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%6d) %-20s %-20s %-25s %-15s %-25s %-13d",
                            order++,
                            rs.getString(1),
                            rs.getString(2),
                            rs.getString(3),
                            rs.getString(4),
                            rs.getString(5),
                            rs.getInt(6));
                    System.out.println();
                }
                System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                System.out.println(ex.getErrorCode());
            }
            //커멘드 입력
            System.out.print("0)뒤로가기 order)해당 회원 상세페이지\n입력 : ");
            command = sc.nextInt();
            if (command == 0)
                return;
            else if (command <= order) {
                order = 1;
                //입력 받은 순번의 user id 받음
                try {
                    rs.first();
                    for (int i = 0; i < command - 1; i++)
                        rs.next();
                    memberDetail(rs.getString(1));
                } catch (SQLException Ex) {
                    System.out.println(Ex.getMessage());
                }
            } else {
                order = 1;
                System.out.println("Invalid command");
            }

        }
    }
}
