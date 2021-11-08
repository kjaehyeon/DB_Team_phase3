import javax.xml.transform.Result;
import java.util.*;
import java.sql.*;

public class Main {
    static String userid;
    static boolean is_admin;
    static ArrayList<Integer> locations = new ArrayList<Integer>();


    public static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    public static final String USER_NAME ="university";
    public static final String USER_PASSWD ="comp322";
    public static Connection conn = null;
    static PreparedStatement pstmt = null;

    public static void main(String[] args) {

        int command;
        Scanner scanner = new Scanner(System.in);

        try {
            conn = DriverManager.getConnection(URL, USER_NAME, USER_PASSWD);
            System.out.println("Connected.");
        }catch(SQLException ex) {
            ex.printStackTrace();
            System.err.println("Cannot get a connection: " + ex.getLocalizedMessage());
            System.err.println("Cannot get a connection: " + ex.getMessage());
            System.exit(1);
        }

        while(true){
                    System.out.println("1)로그인 2)회원가입 3)관리자 로그인 4)프로그램 종료");
                    command = scanner.nextInt();
                    switch (command){
                        case 1:
                            is_admin=true;
                            MainPage();
                            //로그인 페이지
                            break;
                        case 2:
                            SignUpPage();
                            //회원가입 페이지
                            break;
                        case 3:
                            AdminLoginPage();
                            //로그인 페이지
                            break;
                        case 4:
                            System.out.println("프로그램이 종료됩니다.");
                            System.exit(1);
            }
        }
    }
    static int LoginPage(){
        // id, pw받아서 DB에서 확인하고 MainPage로 넘어간다.
        Scanner sc = new Scanner(System.in);
        String userpw = null;
        String sql = null;
        while (true){
            System.out.println("ID와 PW을 입력해주세요");
            System.out.print("ID : ");
            userid = sc.nextLine();
            System.out.print("PW : ");
            userpw = sc.nextLine();
            sql = "SELECT Pw FROM MEMBER WHERE U_id = ?";
            String pw = null; // DB pw
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userid);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){
                    pw = rs.getString(1);
                }
                if (userpw.equals(pw)){
                    break;
                } else{
                    System.out.println("ID 또는 비밀번호가 틀렸습니다");
                    System.out.println("다시 입력해주세요");
                    continue;
                }
            } catch (SQLException ex){
                System.err.println("sql error = " + ex.getMessage());
                System.exit(1);
            }

        }
        try {
            sql = "SELECT Ad_id FROM LIVES_IN WHERE U_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userid);
            ResultSet rs = pstmt.executeQuery();
            int location = 0;
            while(rs.next()){
                location = rs.getInt(1);
                locations.add(location);
            }
        } catch (SQLException ex){
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
        System.out.println("로그인 성공, 메인페이지로 이동합니다.");
        MainPage();
        return 0;
    }
    static int SignUpPage() {
        Scanner sc = new Scanner(System.in);
        String uid = null;
        String upw = null;
        String upw2 = null;
        String name = null;
        String Description = null;
        String tel = null;
        String email = null;
        String sql = null;
        System.out.println("회원가입을 위한 정보를 입력해주세요");
        System.out.print("ID : ");
        uid = sc.nextLine();
        while(true){
            System.out.print("PW : ");
            upw = sc.nextLine();
            System.out.print("PW Confirm: ");
            upw2 = sc.nextLine();
            if (upw.equals(upw2)){
                break;
            } else {
                System.out.println("비밀번호가 일치하지 않습니다. 다시 입력해주세요");
            }
        }
        System.out.print("이름 : ");
        name = sc.nextLine();
        System.out.print("간단 소개 : ");
        Description = sc.nextLine();
        System.out.print("휴대폰 번호(xxxxxxxxxxx): ");
        tel = sc.nextLine();
        System.out.print("email 주소 : ");
        email = sc.nextLine();
        System.out.println("=========================");
        try {
            sql = "INSERT INTO MEMBER (U_id, Pw, Name, Description, Tel, Email) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, uid);
            pstmt.setString(2, upw);
            pstmt.setString(3, name);
            pstmt.setString(4, Description);
            pstmt.setString(5, tel);
            pstmt.setString(6, email);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("회원가입이 완료되었습니다.");
            }
        } catch (SQLException ex){
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }

        return 0;
    }
    static int AdminLoginPage() {
        // id, pw받아서 DB에서 확인하고 MainPage로 넘어간다.
        Scanner sc = new Scanner(System.in);
        String userpw = null;
        String sql = null;
        while (true){
            System.out.println("관리자 ID와 PW을 입력해주세요");
            System.out.print("ID : ");
            userid = sc.nextLine();
            System.out.print("PW : ");
            userpw = sc.nextLine();
            sql = "SELECT Pw FROM ADMIN WHERE Admin_id = ?";
            String pw = null; // DB pw
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, userid);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){
                    pw = rs.getString(1);
                }
                if (userpw.equals(pw)){
                    break;
                } else{
                    System.out.println("ID 또는 비밀번호가 틀렸습니다");
                    System.out.println("다시 입력해주세요");
                    continue;
                }
            } catch (SQLException ex){
                System.err.println("sql error = " + ex.getMessage());
                System.exit(1);
            }
        }
        is_admin = true;
        MainPage();
        return 0;
    }
    static int MainPage(){
        int command;
        Scanner scanner = new Scanner(System.in);
        while(true){
            if(is_admin){
                System.out.println("1)신고 목록 확인 2)회원 목록 확인 3)관리자 추가 4)뒤로 가기 5)프로그램 종료");
                command = scanner.nextInt();
                switch (command){
                    case 1:
                        AdminMainPage.showReportList();
                        break;
                    case 2:
                        AdminMainPage.showUserList();
                        break;
                    case 3:
                        int is_success = AdminMainPage.addAdmin();
                        if(is_success == 0){
                            System.out.println("관리자 추가 성공!");
                        }else{
                            System.out.println("관리자 추가 실패!");
                        }
                        break;
                    case 4:
                        return 0;
                    case 5:
                        System.exit(0);

                }
            }else{
                System.out.println("1)상품 목록 보기 등등");
                command = scanner.nextInt();
                switch (command){
                    case 1:
                        //showItemList();
                        break;
                    case 5: //뒤로가기
                        return 0;
                    case 6: //프로그램 종료
                        System.out.println("프로그램을 종료합니다.");
                        System.exit(0);
                }
            }
        }
    }

}
