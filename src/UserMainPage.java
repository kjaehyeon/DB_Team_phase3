import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class UserMainPage {

    public static PreparedStatement pstmt;

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
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void searchItems(){

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
                        return 0;
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

    }

    public static void registerItem(){

    }

    public static void listBidItems(){

    }
}
