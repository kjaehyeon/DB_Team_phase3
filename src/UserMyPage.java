import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

    public static void listBidItems() {
        int count = 1;
        String sql = "SELECT B.b_id, B.price, B.create_date,"
                + " I.is_end, I.name, I.current_price, I.u_id, I.it_id"
                + " FROM BID B, ITEM I"
                + " WHERE B.it_id = I.it_id"
                + " AND B.u_id = ?";
        try {
            pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pstmt.setString(1, Main.userid);
            ResultSet rs = pstmt.executeQuery();

            System.out.println(String.format("%-5s%-7s%-10s%-20s%-20s%s",
                    "idx",
                    "b_id",
                    "price",
                    "create_date",
                    "bid",
                    "item_name"));
            System.out.println("---------------------------------------------------------------------------------------------");

            String state = null;
            ArrayList<String> bidList = new ArrayList<>();
            String bid = null;

            while (rs.next()) {
                switch (rs.getString(4)) {
                    case "0":
                        state = "In Progress";
                        break;
                    case "1":
                        state = "End Of Bid";
                        break;
                    case "2":
                        state = "Expired";
                        break;
                    case "3":
                        state = "Rating Required";
                        break;
                    case "4":
                        state = "Completed";
                        break;
                }

                if (state.equals("In Progress")) {
                    bid = "In Progress";
                } else if (state.equals("Completed")) {
                    if (rs.getInt(2) == rs.getInt(6)) {
                        bid = "Success";
                    } else {
                        bid = "FAIL";
                    }
                } else {
                    if (rs.getInt(2) == rs.getInt(6)) {
                        bid = "Rating Required";
                    } else {
                        bid = "FAIL";
                    }
                }

                bidList.add(bid);

                System.out.println(String.format("%-5s%-7s%-10s%-20s%-20s%s",
                        count++,
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getDate(3),
                        bid,
                        rs.getString(5)
                ));
            }
            System.out.println();

            Scanner scan = new Scanner(System.in);
            Boolean loop = true;
            while (loop) {
                System.out.println("1)후기 남기기 2)뒤로가기 3)시스템 종료");
                int menu = scan.nextInt();
                switch (menu) {
                    case 1:
                        System.out.println("후기를 남길 인덱스를 입력하세요");
                        int idx = scan.nextInt();

                        if (bidList.get(idx - 1).equals("Rating Required")) {
                            System.out.println("평점을 선택해주세요 0)0점 1)1점 2)2점 3)3점 4)4점 5)5점");

                            int score = scan.nextInt();
                            while (true) {
                                if (score >= 0 && score <= 5) {
                                    break;
                                } else {
                                    System.out.println("다시 입력해주세요");
                                }
                            }
                            scan.nextLine();
                            System.out.println("거래 후기를 남겨주세요");
                            String description = scan.nextLine();

                            rs.first();
                            for (int i = 0; i < idx - 1; i++) {
                                rs.next();
                            }
                            String sellerId = rs.getString(7);

                            sql = "INSERT INTO RATING"
                                    + " VALUES (?, SEQ_RATING.NEXTVAL, ?,"
                                    + " ?, ?)";
                            pstmt = Main.conn.prepareStatement(sql);
                            pstmt.setString(1, sellerId);
                            pstmt.setString(2, Main.userid);
                            pstmt.setInt(3, score);
                            pstmt.setString(4, description);

                            int row_count = pstmt.executeUpdate();
                            if (row_count == 1) {
                                sql = "UPDATE ITEM"
                                        + " SET is_end = 4"
                                        + " WHERE it_id = "
                                        + rs.getInt(8);
                                pstmt = Main.conn.prepareStatement(sql);
                                row_count = pstmt.executeUpdate();
                                if (row_count == 1) {
                                    System.out.println("후기가 등록되었습니다");
                                }
                            } else {
                                System.out.println("후기등록에 실패하였습니다");
                            }
                            pstmt.close();
                            rs.close();
                            System.out.println();
                        } else {
                            System.out.println("후기를 남길수 없는 항목입니다");
                        }
                        loop = false;
                        break;
                    case 2:
                        loop = false;
                        break;
                    case 3:
                        scan.close();
                        pstmt.close();
                        rs.close();
                        Main.conn.close();
                        System.exit(0);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void listUserRatings(){

    }

    public static void listMyItems(){

    }
}
