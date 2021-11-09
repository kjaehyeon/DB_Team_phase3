import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class UserMainPage {

    public static PreparedStatement pstmt;

    public static void selectMenu() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("1)상품목록 보기 2)상품검색 3)회원정보 수정 4)상품 등록하기 5)입찰한 상품 목록 6)시스템 종료");
            int menu = scan.nextInt();
            switch (menu) {
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
                case 6:
                    try {
                        Main.conn.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
            }
        }
    }

    public static void showItemDetail(int item_id) {
        String sql = "SELECT I.it_id, I.name, I.description," +
                " I.min_bid_unit, I.quick_price, I.start_price," +
                " I.current_price, I.create_date, I.expire_Date," +
                " A.Name, C.Name" +
                " FROM ITEM I, CATEGORY C, ADDRESS A" +
                " WHERE I.c_id = C.c_id" +
                " AND I.ad_id = A.ad_id" +
                " AND I.it_id = " + item_id;

        try {
            pstmt = Main.conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.printf("%-20s%s\n", "Item ID", rs.getInt(1));
                System.out.printf("%-20s%s\n", "Item Name", rs.getString(2));
                System.out.printf("%-20s%s\n", "Description", rs.getString(3));
                System.out.printf("%-20s%s\n", "Min Bid Unit", rs.getInt(4));
                System.out.printf("%-20s%s\n", "Quick Price", rs.getInt(5));
                System.out.printf("%-20s%s\n", "Start Price", rs.getInt(6));
                System.out.printf("%-20s%s\n", "Current Price", rs.getInt(7));
                System.out.printf("%-20s%s\n", "Create Date", rs.getDate(8));
                System.out.printf("%-20s%s\n", "Expired Date", rs.getDate(9));
                System.out.printf("%-20s%s\n", "Location", rs.getString(10));
                System.out.printf("%-20s%s\n", "Category", rs.getString(11));
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listItems() {
        int ad_id[] = {1, 2, 3};
        int count = 1;

        String sql = "SELECT it_id, name, u_id"
                + " FROM ITEM"
                + " WHERE ad_id IN (";
        for (int i = 0; i < ad_id.length; i++) {
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

            while (rs.next()) {
                System.out.println(String.format("%-5d%-10d%-20s%s",
                        count++,
                        rs.getInt(1),
                        rs.getString(3),
                        rs.getString(2)));
            }
            System.out.println();

            Scanner scan = new Scanner(System.in);
            while (true) {
                System.out.println("1)아이템 상세 2)뒤로가기 3)시스템 종료");
                int menu = scan.nextInt();
                switch (menu) {
                    case 1:
                        System.out.println("아이템 index를 입력하세요");
                        System.out.print("idx: ");
                        int idx = scan.nextInt();

                        rs.first();
                        for (int i = 0; i < idx - 1; i++) {
                            rs.next();
                        }

                        showItemDetail(rs.getInt(1));
                        break;
                    case 2:
                        scan.close();
                        pstmt.close();
                        return;
                    case 3:
                        scan.close();
                        pstmt.close();
                        Main.conn.close();
                        System.exit(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void searchItems() {

    }

    public static void updateUserInfo() {

    }

    public static void registerItem() {
        String itName;
        String description;
        int minBidUnit;
        int startPrice;
        int quickPrice;
        int expiredDate;
        int adressId;

        Scanner scan = new Scanner(System.in);
        System.out.println("등록할 상품 정보를 입력해주세요");
        System.out.print("Item Name: ");
        itName = scan.nextLine();
        System.out.print("Description: ");
        description = scan.nextLine();
        System.out.print("Min Bid Unit: ");
        minBidUnit = scan.nextInt();
        System.out.print("Start Price: ");
        startPrice = scan.nextInt();
        while (true) {
            System.out.println("Quick Price: ");
            quickPrice = scan.nextInt();
            if (quickPrice < startPrice) {
                System.out.println("즉시구매가는 시작가보다 높아야 합니다");
            } else {
                break;
            }
        }
        System.out.println("Expired Date(yyyy-mm-dd): ");
        while (true){

        }

    }

    public static void listBidItems() {

    }
}
