import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserMainPage {

    public static PreparedStatement pstmt;

    public static void selectMenu(){
        Scanner scan = new Scanner(System.in);
        while(true){
            System.out.println("1)상품목록 보기 2)상품검색 3)회원정보 수정 4)상품 등록하기 5)입찰한 상품 목록");
            int menu = scan.nextInt();
            switch(menu){
                case 1:
                    listItems();
                    break;
                case 2:
                    searchItems();
                    break;
                case 3:
                    updateUserInfo();
                    break;
                case 4:
                    registerItem();
                    break;
                case 5:
                    listBidItems();
                    break;
            }
        }
    }

    public static void listItems(){
        int ad_id[] = {1, 2, 3};
        int count = 1;

        String sql = "SELECT it_id, name, u_id"
                    + " FROM ITEM"
                    + " WHERE ad_id IN (";
        for(int i = 0; i < ad_id.length; i++){
            sql += ad_id[i] + ",";
        }
        sql = sql.replaceFirst(".$", ")");

        try {
            pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = pstmt.executeQuery();

            System.out.println(String.format("%-5s%-10s%-20s%s",
                                            "idx",
                                            "item_id",
                                            "seller_id",
                                            "item_name"));
            System.out.println("--------------------------------------------------------");

            while(rs.next()){
                System.out.println(String.format("%-5d%-10d%-20s%s",
                                    count++,
                                    rs.getInt(1),
                                    rs.getString(3),
                                    rs.getString(2)));
            }

            while(true){

            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void searchItems(){
        Scanner sc = new Scanner(System.in);
        int count = 1;
        String search = null;
        System.out.println("해당 지역 내에 등록된 물품 중에서 검색합니다.");
        System.out.print("검색할 키워드를 입력하세요 : ");
        search = sc.nextLine();
        String sql = "SELECT it_id, u_id, name"
                + " FROM ITEM"
                + " WHERE LOWER(name) LIKE " + String.format("'%%%s%%'", search)
                + " AND ad_id IN (";
        for (int location : Main.locations){
            sql += location + ",";
        }
        sql = sql.replaceFirst(".$", ")");
        try {
            pstmt = Main.conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(String.format("%-5s%-10s%-20s%s",
                    "idx",
                    "item_id",
                    "seller_id",
                    "item_name"));
            System.out.println("--------------------------------------------------------");

            while(rs.next()){
                System.out.println(String.format("%-5d%-10d%-20s%s",
                        count++,
                        rs.getInt(1),
                        rs.getString(3),
                        rs.getString(2)));
            }
        } catch (SQLException ex){
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
        return;
    }

    public static void updateUserInfo(){
        String upw = null;
        String sql = null;
        String pw = null;
        Scanner sc = new Scanner(System.in);
        System.out.println("회원정보를 수정하시려면 PW를 다시 입력해주세요");
        int count = 3;
        while(true) {
            System.out.print("PW : ");
            upw = sc.nextLine();
            try {
                sql = "SELECT Pw FROM MEMBER WHERE U_id = ?";
                pstmt = Main.conn.prepareStatement(sql);
                pstmt.setString(1, Main.userid);
                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){
                    pw = rs.getString(1);
                }
                if (upw.equals(pw)){
                    break;
                } else{
                    System.out.println("비밀번호가 틀렸습니다");
                    if (count == 0){
                        return;
                    }
                    System.out.println("다시 입력해주세요. " + count + "회 남았습니다.");
                    count--;
                    continue;
                }
            } catch (SQLException ex){
                System.err.println("sql error = " + ex.getMessage());
                System.exit(1);
            }
        }
        //여기서 회원정보 수정
        // 비밀번호 변경
        while (true){
            System.out.print("변경할 PW : ");
            upw = sc.nextLine();
            System.out.println("PW 확인 : ");
            pw = sc.nextLine();
            if (upw.equals(pw)){
                break;
            } else {
                System.out.println("다시 입려해주세요");
            }
        }
        // 이름 변경
        System.out.print("변경할 이름 : ");
        String name = sc.nextLine();
        // 한 줄 소개 변경
        System.out.println("변경할 소개글 : ");
        String description = sc.nextLine();
        // 전화번호 변경
        System.out.println("변경할 휴대폰 번호 : ");
        String tel = sc.nextLine();
        // email 변경
        System.out.println("변경할 email : ");
        String email = sc.nextLine();

        try {
            sql = "UPDATE MEMBER SET Pw = ?, Name = ?, Description = ?, Tel = ?, Email = ? WHERE U_id = ?;";
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setString(1, upw);
            pstmt.setString(2, name);
            pstmt.setString(3, description);
            pstmt.setString(4, tel);
            pstmt.setString(5, email);
            pstmt.setString(6, Main.userid);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("회원정보 변경이 완료되었습니다.");
            }
        } catch (SQLException ex){
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
        return;
    }

    public static void registerItem(){

    }

    public static void listBidItems(){

    }
}
