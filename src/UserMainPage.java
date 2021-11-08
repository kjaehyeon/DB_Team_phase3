import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    }

    public static void registerItem(){

    }

    public static void listBidItems(){

    }
}
