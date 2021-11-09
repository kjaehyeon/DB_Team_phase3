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
                " I.is_end, A.Name, C.Name" +
                " FROM ITEM I, CATEGORY C, ADDRESS A" +
                " WHERE I.c_id = C.c_id" +
                " AND I.ad_id = A.ad_id" +
                " AND I.is_end = '0'" +
                " AND I.it_id = " + item_id;

        try {
            pstmt = Main.conn.prepareStatement(sql);

            ResultSet rs = pstmt.executeQuery();
            char is_completed;
            while (rs.next()) {
                if(rs.getString(10).equals("1")){
                    is_completed = 'o';
                } else{
                    is_completed = 'x';
                }
                System.out.printf("%-20s%s\n", "Item ID", rs.getInt(1));
                System.out.printf("%-20s%s\n", "Item Name", rs.getString(2));
                System.out.printf("%-20s%s\n", "Description", rs.getString(3));
                System.out.printf("%-20s%s\n", "Min Bid Unit", rs.getInt(4));
                System.out.printf("%-20s%s\n", "Quick Price", rs.getInt(5));
                System.out.printf("%-20s%s\n", "Start Price", rs.getInt(6));
                System.out.printf("%-20s%s\n", "Current Price", rs.getInt(7));
                System.out.printf("%-20s%s\n", "Create Date", rs.getDate(8));
                System.out.printf("%-20s%s\n", "Expired Date", rs.getDate(9));
                System.out.printf("%-20s%s\n", "Is Completed", is_completed);
                System.out.printf("%-20s%s\n", "Location", rs.getString(11));
                System.out.printf("%-20s%s\n", "Category", rs.getString(12));
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

        String sql = "SELECT it_id, u_id, expire_date, is_end, name"
                + " FROM ITEM"
                + " WHERE ad_id IN (";
        for (int i = 0; i < ad_id.length; i++) {
            sql += ad_id[i] + ",";
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
                if(rs.getDate(3).toLocalDate().isBefore((now))){
                    is_expired = 'o';
                } else{
                    is_expired = 'x';
                }
                if(rs.getString(4).equals("1")){
                    is_completed = 'o';
                } else{
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
                        System.exit(1);
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
                if(rs.getDate(3).toLocalDate().isBefore((now))){
                    is_expired = 'o';
                } else{
                    is_expired = 'x';
                }
                if(rs.getString(4).equals("1")){
                    is_completed = 'o';
                } else{
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

    public static void updateUserInfo() {
        String upw = null;
        String sql = null;
        String pw = null;
        Scanner sc = new Scanner(System.in);
        System.out.println("회원정보를 수정하시려면 PW를 다시 입력해주세요");
        int count = 3;
        while (true) {
            System.out.print("PW : ");
            upw = sc.nextLine();
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
            upw = sc.nextLine();
            System.out.println("PW 확인 : ");
            pw = sc.nextLine();
            if (upw.equals(pw)) {
                break;
            } else {
                System.out.println("다시 입려해주세요");
            }
        }
        // 이름 변경
        System.out.print("변경할 이름 : ");
        String name = sc.nextLine();
        // 한 줄 소개 변경
        System.out.print("변경할 소개글 : ");
        String description = sc.nextLine();
        // 전화번호 변경
        System.out.print("변경할 휴대폰 번호 : ");
        String tel = sc.nextLine();
        // email 변경
        System.out.print("변경할 email : ");
        String email = sc.nextLine();

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
            System.out.print("Address Id 1)동인동 2)삼덕동 3)성내1동: ");
            adressId = scan.nextInt();
            if (adressId >= 1 && adressId <= 3) {
                break;
            } else {
                System.out.println("부적절한 값입니다");
            }
        }

        while (true) {
            System.out.print("Category Id 1)디지털기기 2)생활가전 3)가구/인테리어: ");
            categoryId = scan.nextInt();
            if (categoryId >= 1 && categoryId <= 3) {
                break;
            } else {
                System.out.println("부적절한 값입니다");
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
            pstmt.setString(9, "kKTixmkxQM");
            pstmt.setString(10, adminList.get(randInt));
            pstmt.setInt(11, adressId);

            int rowCount = pstmt.executeUpdate();
            if (rowCount == 1) {
                System.out.println("아이템이 등록되었습니다");
            } else{
                System.out.println("아이템 등록에 실패하였습니다");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void listBidItems() {

    }
}
