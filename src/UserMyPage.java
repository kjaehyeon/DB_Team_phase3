import java.sql.PreparedStatement;
import java.util.Scanner;

public class UserMyPage {
    public static PreparedStatement pstmt;
    public static Scanner scan = new Scanner(System.in);

    public static void selectMenu(){
        System.out.println("1)입찰목록 조회 2)평점 조회 3)등록한 아이템 조회");
        int menu = scan.nextInt();
        switch (menu){
            case 1:
                listBidItems();
                break;
            case 2:
                listUserRatings();
                break;
            case 3:
                listMyItems();
                break;
        }
    }

    public static void listBidItems(){

    }

    public static void listUserRatings(){

    }

    public static void listMyItems(){

    }
}
