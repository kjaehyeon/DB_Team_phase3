import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UserMainPage {

    public static PreparedStatement pstmt;

    public static void selectMenu() {
        Scanner scan = new Scanner(System.in);
        boolean loop = true;
        while (loop) {
            System.out.println("1)상품목록 보기 2)상품검색 3)마이페이지 4)상품 등록하기 5)신고되지 않은 상품만 보기 6)시스템 종료");
            int menu = scan.nextInt();
            switch (menu) {
                case 1:
                    listItems();
                    break;
                case 2:
                    searchItems();
                    break;
                case 3:
                    UserMyPage.selectMenu();
                    if (Main.log_in == false){
                        loop = false;
                    }
                    break;
                case 4:
                    registerItem();
                    break;
                case 5:
                    not_reported_item();
                    break;
                case 6:
                    try {
                        Main.conn.close();
                        scan.close();
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
                    case "4":
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

            ArrayList<String> adminList = new ArrayList<>();
            Random random = new Random();
            random.setSeed(System.currentTimeMillis());

            Statement stmt = Main.conn.createStatement();
            sql = "SELECT admin_id"
                    + " FROM ADMIN";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                adminList.add(rs.getString(1));
            }

            Scanner scan = new Scanner(System.in);
            System.out.println("1)입찰하기 2)즉시 구매하기 3)신고하기 4)뒤로가기 5)시스템 종료");
            int menu = scan.nextInt();
            switch (menu) {
                case 1:
                    if (!state.equals("In Progress")) {
                        System.out.println("입찰할 수 없는 아이템입니다");
                        break;
                    }
                    int price = 0;
                    while (true) {
                        System.out.printf("입찰가를 입력해주세요(최소 입찰 단위: %d원): ", minBidUnit);
                        price = scan.nextInt();

                        if (price <= currentPrice) {
                            System.out.println("입찰가는 현재가보다 커야합니다");
                        } else {
                            if (((currentPrice - price) % minBidUnit) == 0) {
                                break;
                            } else {
                                System.out.println("최소 입찰 단위를 맞춰주세요");
                            }
                        }
                    }

                    sql = "INSERT INTO BID"
                            + " VALUES (SEQ_BID.NEXTVAL, ?, SYSDATE,"
                            + " ?, ?)";
                    pstmt = Main.conn.prepareStatement(sql);
                    pstmt.setInt(1, price);
                    pstmt.setString(2, Main.userid);
                    pstmt.setInt(3, item_id);

                    int row_count = pstmt.executeUpdate();
                    if (row_count == 1) {
                        sql = "UPDATE ITEM"
                                + " SET current_price = ?"
                                + " WHERE it_id = ?";
                        pstmt = Main.conn.prepareStatement(sql);
                        pstmt.setInt(1, price);
                        pstmt.setInt(2, item_id);

                        row_count = pstmt.executeUpdate();
                        if (row_count == 1) {
                            System.out.println("입찰에 성공하였습니다");
                        } else {
                            System.out.println("현재가 업데이트에 실패하였습니다");
                        }
                    } else {
                        System.out.println("입찰에 실패하였습니다");
                    }
                    break;
                case 2:
                    if (!state.equals("In Progress")) {
                        System.out.println("즉시 구매할 수 없는 아이템입니다");
                        break;
                    }
                    sql = "INSERT INTO BID"
                            + " VALUES (SEQ_BID.NEXTVAL, ?, SYSDATE,"
                            + " ?, ?)";
                    pstmt = Main.conn.prepareStatement(sql);
                    pstmt.setInt(1, quickPrice);
                    pstmt.setString(2, Main.userid);
                    pstmt.setInt(3, item_id);
                    pstmt.executeUpdate();

                    sql = "UPDATE ITEM"
                            + " SET current_price = ?,"
                            + " is_end = 1"
                            + " WHERE it_id = ?";
                    pstmt = Main.conn.prepareStatement(sql);
                    pstmt.setInt(1, quickPrice);
                    pstmt.setInt(2, itemId);

                    row_count = pstmt.executeUpdate();
                    if (row_count == 1) {
                        System.out.println("즉시 구매하였습니다");
                    } else {
                        System.out.println("구매 실패하였습니다");
                    }
                    break;
                case 3:
                    System.out.println("신고내용을 작성해주세요");
                    scan.nextLine();
                    String description = scan.nextLine();

                    sql = "INSERT INTO REPORT"
                            + " VALUES (SEQ_REPORT.NEXTVAL, ?, ?,"
                            + " ?, ?)";

                    pstmt = Main.conn.prepareStatement(sql);
                    pstmt.setString(1, description);
                    pstmt.setString(2, Main.userid);
                    pstmt.setInt(3, itemId);
                    pstmt.setString(4, adminList.get(random.nextInt(adminList.size())));
                    row_count = pstmt.executeUpdate();
                    if (row_count == 1) {
                        System.out.println("신고완료 되었습니다");
                    } else {
                        System.out.println("신고를 실패하였습니다");
                    }
                    break;
                case 4:
                    break;
                case 5:
                    scan.close();
                    rs.close();
                    pstmt.close();
                    System.exit(0);
                    break;
            }

            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listItems() {
        int count = 1;

        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE ad_id IN (";
        for (int location : Main.locations) {
            sql += location + ",";
        }
        sql = sql.replaceFirst(".$", ")");

        try {
            pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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
                    case "4":
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
                        pstmt.close();
                        return;
                    case 3:
                        scan.close();
                        pstmt.close();
                        Main.conn.close();
                        System.exit(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void searchItems() {
        Scanner sc = new Scanner(System.in);
        int count = 1;
        String search = null;
        System.out.println("해당 지역 내에 등록된 물품 중에서 검색합니다.");
        System.out.print("검색할 키워드를 입력하세요 : ");
        search = sc.nextLine();
        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE LOWER(name) LIKE " + String.format("'%%%s%%'", search)
                + " AND ad_id IN (";
        for (int location : Main.locations) {
            sql += location + ",";
        }
        sql = sql.replaceFirst(".$", ")");
        try {
            pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = pstmt.executeQuery();
            System.out.println(String.format("%-5s%-10s%-20s%-15s%-15s%s",
                    "idx",
                    "item_id",
                    "seller_id",
                    "is_expired",
                    "is_completed",
                    "item_name"));
            System.out.println("------------------------------------------------------------------------------");

            LocalDate now = LocalDate.now();
            char is_expired;
            char is_completed;
            while (rs.next()) {
                if (rs.getDate(3).toLocalDate().isBefore((now))) {
                    is_expired = 'o';
                } else {
                    is_expired = 'x';
                }
                if (rs.getString(4).equals("1")) {
                    is_completed = 'o';
                } else {
                    is_completed = 'x';
                }
                System.out.println(String.format("%-5s%-10s%-20s%-15s%-15s%s",
                        count++,
                        rs.getInt(1),
                        rs.getString(2),
                        is_expired,
                        is_completed,
                        rs.getString(5)
                ));
            }
            System.out.println();
            while (true) {
                System.out.println("1)아이템 상세 2)뒤로가기 3)시스템 종료");
                int menu = sc.nextInt();
                switch (menu) {
                    case 1:
                        System.out.println("아이템 index를 입력하세요");
                        System.out.print("idx: ");
                        int idx = sc.nextInt();

                        rs.first();
                        for (int i = 0; i < idx - 1; i++) {
                            rs.next();
                        }

                        showItemDetail(rs.getInt(1));
                        break;
                    case 2:
                        pstmt.close();
                        return;
                    case 3:
                        sc.close();
                        pstmt.close();
                        Main.conn.close();
                        System.exit(1);
                }
            }
        } catch (SQLException ex) {
            System.err.println("sql error = " + ex.getMessage());
            System.exit(1);
        }
        return;
    }


    public static void registerItem() {
        String itName;
        String description;
        int minBidUnit;
        int startPrice;
        int quickPrice;
        String expiredDate;
        int adressId;
        int categoryId;

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
            System.out.print("Quick Price: ");
            quickPrice = scan.nextInt();
            if (quickPrice < startPrice) {
                System.out.println("즉시구매가는 시작가보다 높아야 합니다");
            } else {
                break;
            }
        }
        scan.nextLine();
        while (true) {
            System.out.print("Expired Date(yyyy-mm-dd): ");
            expiredDate = scan.nextLine();

            boolean check = Pattern.matches(
                    "(19|20)\\d{2}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])",
                    expiredDate);
            if (!check) {
                System.out.println("잘못된 형식입니다");
                continue;
            }

            LocalDate now = LocalDate.now();
            LocalDate expired = LocalDate.parse(expiredDate);

            if (expired.isBefore(now)) {
                System.out.println("만료 날짜는 현재 날짜 이후여야 합니다");
            } else {
                break;
            }
        }
        while (true) {
            try{
                String sql = "select ad_id,name from address where ad_id in (";
                for (int location : Main.locations) {
                    sql += location + ",";
                }
                sql = sql.replaceFirst(".$", ")");
                pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = pstmt.executeQuery(sql);
                System.out.print("Address Id ");
                int index = 1;
                while (rs.next()){
                    System.out.print(index++ + ")"+rs.getString(2)+" ");
                }
                int idx = scan.nextInt();
                if(idx < index && idx > 0){
                    rs.first();
                    for(int i =0; i< idx-1; i++)
                        rs.next();
                    adressId = rs.getInt(1);
                    break;
                }else {
                    System.out.println("부적절한 값입니다");
                    continue;
                }
            }catch (SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
        }
        while (true) {
            try{
                String sql = "select c_id, name from category";
                pstmt = Main.conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = pstmt.executeQuery(sql);
                System.out.print("Category ID ");
                int index = 1;
                while (rs.next()){
                    System.out.print(index++ +")"+rs.getString(2)+" ");
                }
                System.out.println();
                int idx = scan.nextInt();
                if(idx < index && idx > 0){
                    rs.first();
                    for(int i =0; i< idx-1; i++)
                        rs.next();
                    categoryId = rs.getInt(1);
                    break;
                } else {
                    System.out.println("부적절한 값입니다");
                }
            }catch (SQLException sqlException){
                System.out.println(sqlException.getMessage());
            }
        }
        try {
            ArrayList<String> adminList = new ArrayList<>();
            Random random = new Random();
            random.setSeed(System.currentTimeMillis());

            Statement stmt = Main.conn.createStatement();
            String sql = "SELECT admin_id"
                    + " FROM ADMIN";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                adminList.add(rs.getString(1));
            }
            int randInt = random.nextInt(adminList.size());

            sql = "INSERT INTO ITEM(it_id, name, description,"
                    + " min_bid_unit, quick_price, current_price,"
                    + " expire_date, start_price, img, c_id,"
                    + " u_id, admin_id, ad_id)"
                    + " VALUES (SEQ_ITEM.NEXTVAL, ?, ?,"
                    + " ?, ?, ?,"
                    + " ?, ?, EMPTY_BLOB(), ?,"
                    + " ?, ?, ?)";

            pstmt = Main.conn.prepareStatement(sql);
            pstmt.setString(1, itName);
            pstmt.setString(2, description);
            pstmt.setInt(3, minBidUnit);
            pstmt.setInt(4, quickPrice);
            pstmt.setInt(5, startPrice);
            pstmt.setDate(6, Date.valueOf(expiredDate));
            pstmt.setInt(7, startPrice);
            pstmt.setInt(8, categoryId);
            pstmt.setString(9, Main.userid);
            pstmt.setString(10, adminList.get(randInt));
            pstmt.setInt(11, adressId);

            int rowCount = pstmt.executeUpdate();
            if (rowCount == 1) {
                System.out.println("아이템이 등록되었습니다");
            } else {
                System.out.println("아이템 등록에 실패하였습니다");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static void not_reported_item(){
        ResultSet rs;
        String query;
        try{
            query = "select  i.it_id,\n" +
                    "        i.name,\n" +
                    "        i.current_price\n" +
                    "from   item i\n" +
                    "where i.ad_id in (";
            for (int location : Main.locations) {
                query += location + ",";
            }
            query = query.replaceFirst(".$", ")");
            query = query + "minus\n" +
                    "select  i.it_id,\n" +
                    "        i.name,\n" +
                    "        i.current_price\n" +
                    "from    item i\n" +
                    "where   i.it_id IN (select  r.it_id\n" +
                    "                    from    report r)\n" +
                    "        and i.ad_id in (";
            for (int location : Main.locations) {
                query += location + ",";
            }
            query = query.replaceFirst(".$", ")");
            pstmt = Main.conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            System.out.println("신고가 접수되지 않은 상품입니다.\n아이템 상세 정보는 검색 기능을 이용해 주세요");
            System.out.println("ITEM NAME                      Current_Price");
            System.out.println("--------------------------------------------");
            while(rs.next()){
                System.out.printf("%-30s %d", rs.getString(2), rs.getInt(3));
            }
            System.out.println();
        }catch (SQLException sqlException){
            System.out.println(sqlException.getMessage());
        }

    }
}
