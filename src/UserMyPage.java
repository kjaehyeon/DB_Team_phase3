import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class UserMyPage {
    public static PreparedStatement pstmt;
    public static Scanner scan = new Scanner(System.in);

    public static void selectMenu(){
        Boolean loop = true;
        while(loop) {
            System.out.println("1)입찰목록 조회 2)평점 조회 3)등록한 아이템 조회 4)회원정보 수정 5)뒤로 가기");
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
                case 4:
                    updateUserInfo();
                    break;
                case 5:
                    loop = false;
                    break;
            }
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
        int count = 1;
        System.out.println("===== 평점 목록 =====");
        String sql = "SELECT Buy_id, Score, Description" +
                " FROM RATING" +
                " WHERE s_id = ?";
        System.out.println(String.format("%-5s%-20s%-5s%s",
                "idx",
                "Buyer_id",
                "Score",
                "Description"));
        System.out.println("------------------------------------------------------------------------------");
        try {
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setString(1, Main.userid);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                System.out.println(
                        String.format("%-5s%-20s%-5d%s",
                                count++,
                                rs.getString(1),
                                rs.getInt(2),
                                rs.getString(3)
                        )
                );
            }
            System.out.println("------------------------------------------------------------------------------");
            sql = "SELECT s_id, AVG(Score)" +
                    " FROM Rating" +
                    " WHERE s_id = ?" +
                    " GROUP BY s_id";
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setString(1, Main.userid);
            rs = pstmt.executeQuery();
            while(rs.next()){
                System.out.println("평균 평점 : " + rs.getDouble(2));
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void listMyItems(){
        boolean loop = true;
        while(loop) {
            System.out.println("1)판매 중 상품 조회 2)낙찰 상품 조회 3)기간만료 상품 조회 4)거래완료 상품 조회 5)뒤로 가기");
            int menu = scan.nextInt();
            switch (menu){
                case 1:
                    listMyInProgressItems();
                    break;
                case 2:
                    listMyEndOfBidItems();
                    break;
                case 3:
                    listMyExpiredItems();
                    break;
                case 4:
                    listCompletedItems();
                    break;
                case 5:
                    loop = false;
                    break;
            }
        }
    }
    public static void updateUserInfo() {
        String upw = null;
        String sql = null;
        String pw = null;
        System.out.println("회원정보를 수정하시려면 PW를 다시 입력해주세요");
        int count = 3;
        while (true) {
            System.out.print("PW : ");
            upw = scan.nextLine();
            try {
                sql = "SELECT Pw FROM MEMBER WHERE U_id = ?";
                pstmt = Main.conn.prepareStatement(sql);
                pstmt.setString(1, Main.userid);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    pw = rs.getString(1);
                }
                if (upw.equals(pw)) {
                    break;
                } else {
                    System.out.println("비밀번호가 틀렸습니다");
                    if (count == 0) {
                        return;
                    }
                    System.out.println("다시 입력해주세요. " + count + "회 남았습니다.");
                    count--;
                    continue;
                }
            } catch (SQLException ex) {
                System.err.println("sql error = " + ex.getMessage());
                System.exit(1);
            }
        }
        //여기서 회원정보 수정
        // 비밀번호 변경
        while (true) {
            System.out.print("변경할 PW : ");
            upw = scan.nextLine();
            System.out.println("PW 확인 : ");
            pw = scan.nextLine();
            if (upw.equals(pw)) {
                break;
            } else {
                System.out.println("다시 입려해주세요");
            }
        }
        // 이름 변경
        System.out.print("변경할 이름 : ");
        String name = scan.nextLine();
        // 한 줄 소개 변경
        System.out.print("변경할 소개글 : ");
        String description = scan.nextLine();
        // 전화번호 변경
        System.out.print("변경할 휴대폰 번호 : ");
        String tel = scan.nextLine();
        // email 변경
        System.out.print("변경할 email : ");
        String email = scan.nextLine();

        try {
            sql = "UPDATE MEMBER SET Pw = ?, Name = ?, Description = ?, Tel = ?, Email = ? WHERE U_id = ?";
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
        } catch (SQLException ex) {
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
        return;
    }
    public static void listMyEndOfBidItems() {
        int count = 1;
        System.out.println("===== 낙찰 완료 상품 목록 =====");
        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE is_end = 1 AND u_id = ?";
        listMyItemsList(sql);
    }
    public static void listCompletedItems() {
        int count = 1;
        System.out.println("===== 거래 완료 상품 목록 =====");
        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE is_end = 3 OR is_end = 4 AND u_id = ?";
        listMyItemsList(sql);
    }
    public static void listMyExpiredItems() {
        int count = 1;
        System.out.println("===== 기간 만료 상품 목록 =====");
        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE is_end = 2 AND u_id = ?";
        listMyItemsList(sql);
    }
    public static void listMyInProgressItems() {
        int count = 1;
        System.out.println("===== 기간 만료 상품 목록 =====");
        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE is_end = 0 AND u_id = ?";
        listMyItemsList(sql);
    }

    public static void listMyItemsList(String sql){
        int count = 1;
        try {
            pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            pstmt.setString(1, Main.userid);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(String.format("%-5s%-10s%-20s%-15s%s",
                    "idx",
                    "item_id",
                    "seller_id",
                    "state",
                    "item_name"));
            System.out.println("------------------------------------------------------------------------------");

            String state = null;

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
                        state = "Completed";
                        break;
                }
                System.out.println(String.format("%-5s%-10s%-20s%-15s%s",
                        count++,
                        rs.getInt(1),
                        rs.getString(2),
                        state,
                        rs.getString(5)
                ));
            }
            System.out.println();
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

                        showMyItemDetail(rs.getInt(1), rs.getString(4));
                        break;
                    case 2:
                        pstmt.close();
                        return;
                    case 3:
                        scan.close();
                        pstmt.close();
                        Main.conn.close();
                        System.exit(0);
                }
            }
        } catch (SQLException ex) {
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
    }


    public static void showMyItemDetail(int item_id, String is_end) {
        String sql = "SELECT I.it_id, I.name, I.description," +
                " I.min_bid_unit, I.quick_price, I.start_price," +
                " I.current_price, I.create_date, I.expire_Date," +
                " I.is_end, A.name, C.name, M.average_score" +
                " FROM ITEM I, CATEGORY C, ADDRESS A, MEMBER M" +
                " WHERE I.c_id = C.c_id" +
                " AND I.ad_id = A.ad_id" +
                " AND I.u_id = M.u_id" +
                " AND I.it_id = " + item_id;

        try {
            pstmt = Main.conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            String state = null;
            int itemId = 0;
            int minBidUnit = 0;
            int quickPrice = 0;
            int currentPrice = 0;
            while (rs.next()) {
                switch (rs.getString(10)) {
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
                        state = "Completed";
                        break;
                }
                itemId = rs.getInt(1);
                minBidUnit = rs.getInt(4);
                quickPrice = rs.getInt(5);
                currentPrice = rs.getInt(7);
                System.out.printf("%-20s%s\n", "Item ID", itemId);
                System.out.printf("%-20s%s\n", "Item Name", rs.getString(2));
                System.out.printf("%-20s%s\n", "Description", rs.getString(3));
                System.out.printf("%-20s%s\n", "Min Bid Unit", minBidUnit);
                System.out.printf("%-20s%s\n", "Quick Price", quickPrice);
                System.out.printf("%-20s%s\n", "Start Price", rs.getInt(6));
                System.out.printf("%-20s%s\n", "Current Price", currentPrice);
                System.out.printf("%-20s%s\n", "Create Date", rs.getDate(8));
                System.out.printf("%-20s%s\n", "Expired Date", rs.getDate(9));
                System.out.printf("%-20s%s\n", "State", state);
                System.out.printf("%-20s%s\n", "Location", rs.getString(11));
                System.out.printf("%-20s%s\n", "Category", rs.getString(12));
                System.out.printf("%-20s%f\n", "Seller Score", rs.getDouble(13));
            }
            System.out.println();

            switch (is_end) {
                case "0":
                    System.out.println("1)삭제하기 2)기간 연장하기 3)뒤로 가기");
                    int menu0 = scan.nextInt();
                    switch (menu0) {
                        case 1:
                            DeleteItem(item_id);
                            break;
                        case 2:
                            System.out.println("연장할 기간을 입력해주세요(2021-12-31)");
                            String extendedDate = scan.nextLine();
                            ExtendDate(item_id, extendedDate);
                            break;
                        case 3:
                            break;
                        default:
                            System.out.println("잘못 입력하셨습니다. 뒤로갑니다.");
                            break;
                    }
                    break;
                case "1":
                    System.out.println("1)거래 완료하기 2)뒤로 가기");
                    int menu1 = scan.nextInt();
                    switch (menu1) {
                        case 1:
                            CompleteItem(item_id);
                            break;
                        case 2:
                            break;
                        default:
                            System.out.println("잘못 입력하셨습니다. 뒤로갑니다.");
                            break;
                    }
                    break;
                case "2":
                    System.out.println("1)기간 연장하기 2)삭제하기 3)거래 완료하기 4)뒤로가기");
                    int menu2 = scan.nextInt();
                    switch (menu2) {
                        case 1:
                            System.out.println("연장할 기간을 입력해주세요(2021-12-31)");
                            String extendedDate = scan.nextLine();
                            ExtendDate(item_id, extendedDate);
                            break;
                        case 2:
                            DeleteItem(item_id);
                            break;
                        case 3:
                            CompleteItem(item_id);
                            break;
                        case 4:
                            break;
                        default:
                            System.out.println("잘못 입력하셨습니다. 뒤로갑니다.");
                            break;
                    }
                    break;
                case "3":
                    System.out.println("1)뒤로 가기");
                    int menu3 = scan.nextInt();
                    switch (menu3) {
                        case 1:
                            break;
                        default:
                            System.out.println("잘못 입력하셨습니다. 뒤로갑니다.");
                            break;
                    }
                    break;
                case "4":
                    System.out.println("1)후기 보기 2)뒤로 가기");
                    int menu4 = scan.nextInt();
                    switch (menu4) {
                        case 1:
                            ShowRating(item_id);
                            break;
                        case 2:
                            break;
                        default:
                            System.out.println("잘못 입력하셨습니다. 뒤로갑니다.");
                            break;
                    }
                    break;
                }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void DeleteItem(int item_id) {
        String sql = "DELETE FROM BID WHERE it_id = ? ";
        try {
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setInt(1, item_id);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("성공적으로 상품에 관련된 입찰들을 지웠습니다");
            } else {
                System.out.println("상품에 관련된 입찰들을 지우는데 실패하였습니다");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM ITEM WHERE it_id = ?";
        try {
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setInt(1, item_id);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("성공적으로 상품을 지웠습니다");
            } else {
                System.out.println("상품을 지우는데 실패하였습니다");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static void ExtendDate(int item_id, String extendedDate){
        String sql = "UPDATE ITEM SET expired_date = ? WHERE It_id = ?";
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null;
        try {
            dt = (Date) fm.parse(extendedDate);
        }catch (Exception e){
            System.out.println("날짜 변환 실패");
            e.printStackTrace();
        }

        try {
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setDate(1, dt);
            pstmt.setInt(2, item_id);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("성공적으로 상품의 기간을 연장했습니다");
            } else {
                System.out.println("상품의 기간을 연장하는데 실패하였습니다");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void CompleteItem(int item_id){
        String sql = "UPDATE ITEM SET is_end = 3 WHERE It_id = ?";

        try {
            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setInt(1, item_id);
            int state = pstmt.executeUpdate();
            if (state == 1) {
                System.out.println("성공적으로 거래를 완료했습니다. 구매자가 상품에 대한 후기를 쓸 예정입니다");
            } else {
                System.out.println("거래완료에 실패하였습니다");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void ShowRating(int item_id){
        //잠시보류
        System.out.println("상품별 후기 보기는 준비중에 있습니다.");
        //String sql = "SELECT "
    }
}
