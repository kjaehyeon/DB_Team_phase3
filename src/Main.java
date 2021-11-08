import java.util.*;
import java.sql.*;

public class Main {
    static String userid;
    static String userpw;
    static boolean is_admin;

    public static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
    public static final String USER_NAME ="DBTeam7";
    public static final String USER_PASSWD ="comp322";
    public static Connection conn = null;

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
                    System.out.println("1)로그인 2)회원가입 3)프로그램 종료");
                    command = scanner.nextInt();
                    switch (command){
                        case 1:
                            is_admin=true;
                            MainPage();
                            //로그인 페이지
                            break;
                        case 2:
                            //회원가입 페이지
                            break;
                        case 3:
                            System.out.println("프로그램이 종료됩니다.");
                            System.exit(1);
            }
        }
    }
    static int LoginPage(){
        // id, pw받아서 DB에서 확인하고 MainPage로 넘어간다.
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
