package kiosk;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

// 메뉴 항목의 데이터 모델 - 각 상품의 정보를 캡슐화
class MenuItem {
    // 상품의 기본 속성들
    private String name;       // 상품명
    private int price;         // 상품 가격
    private String category;   // 상품 카테고리
    private String imagePath;  // 상품 이미지 경로
    private int quantity;      // 장바구니 수량 (기본값 1)

    // 생성자: 새로운 메뉴 아이템을 초기화
    public MenuItem(String name, int price, String category, String imagePath) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.quantity = 1;  // 장바구니에 추가될 때 기본 수량은 1
    }

    // 이하 getter와 setter 메서드들 - 클래스의 속성에 안전하게 접근하고 수정
    public String getName() {
        return name;
    }
    
    // ... (다른 getter/setter 메서드들은 동일한 패턴)

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

// 키오스크의 메인 클래스 - 전체 애플리케이션의 로직과 UI 담당
public class kiosk extends JFrame {
    // 애플리케이션 전체에서 공유되는 정적 컬렉션과 변수들
    private static ArrayList<MenuItem> menuList = new ArrayList<>();     // 전체 메뉴 리스트
    private static ArrayList<MenuItem> cart = new ArrayList<>();         // 장바구니 리스트
    private static JLabel totalLabel;                                   // 총 금액 표시 레이블
    private static JPanel menuPanel;                                    // 메뉴 항목들을 표시할 패널
    private static boolean isAdminLoggedIn = false;                     // 관리자 로그인 상태

    // 장바구니 영역을 위한 스크롤 가능한 패널
    private JPanel cartAreaPanel;  

 // 애플리케이션의 진입점 - 초기 메뉴 설정 및 UI 생성
    public static void main(String[] args) {
        addSampleMenus(); // 초기 샘플 메뉴 추가
        
        // Swing UI는 이벤트 디스패치 스레드에서 생성 (스레드 안전성 보장)
        SwingUtilities.invokeLater(kiosk::new);
    }

    // 초기 메뉴 항목들을 프로그램 시작 시 추가
    private static void addSampleMenus() {
        // 라이온즈 유니폼 관련 초기 메뉴 아이템 추가
        menuList.add(new MenuItem("2024레플리카선데이마킹시트", 22000, "유니폼", "src/유니폼/2024레플리카선데이마킹시트.png"));
        menuList.add(new MenuItem("2024레플리카어웨이유니폼", 79000, "유니폼", "src/유니폼/2024레플리카어웨이유니폼.png"));
        menuList.add(new MenuItem("2024레플리카홈유니폼.png", 79000, "유니폼", "src/유니폼/2024레플리카홈유니폼.png"));
        menuList.add(new MenuItem("2024프로페셔널선데이마킹시트", 22000, "유니폼", "src/유니폼/2024프로페셔널선데이마킹시트.png"));
    }
 
    // 키오스크 생성자 - GUI 컴포넌트 초기화 및 레이아웃 설정
    public kiosk() {
        setTitle("라이온즈 키오스크");  // 애플리케이션 타이틀 설정
        setSize(800, 600);  // 창 크기 설정
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 창 닫기 동작 설정
        setLayout(new BorderLayout());  // 레이아웃 관리자 설정

        // 카테고리 버튼 패널 생성 - 상품 카테고리 필터링
        JPanel categoryPanel = new JPanel(new FlowLayout());
        String[] categories = {"유니폼", "의류", "모자", "가방", "야구용품", "응원용품", "기타상품"};
        
        // 각 카테고리 버튼 생성 및 클릭 시 해당 카테고리 메뉴 표시
        for (String category : categories) {
            JButton categoryButton = new JButton(category);
            categoryButton.addActionListener(e -> showMenuByCategory(category));
            categoryPanel.add(categoryButton);
        }

        // 메뉴 패널 설정 - 2행 4열 그리드 레이아웃
        menuPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // 장바구니 영역 패널 설정 - 세로 박스 레이아웃
        cartAreaPanel = new JPanel();
        cartAreaPanel.setLayout(new BoxLayout(cartAreaPanel, BoxLayout.Y_AXIS));

        // 총 금액 레이블 생성 - 중앙 정렬
        totalLabel = new JLabel("총 금액: 0원", SwingConstants.CENTER);

        // 장바구니 패널 구성 - 스크롤 가능한 장바구니와 총 금액 표시
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.add(new JScrollPane(cartAreaPanel), BorderLayout.CENTER);  // 스크롤 추가
        cartPanel.add(totalLabel, BorderLayout.SOUTH);

        // 결제 및 관리자 모드 버튼 패널 구성
        JPanel buttonPanel = new JPanel(new BorderLayout());
        JButton checkoutButton = new JButton("결제");
        checkoutButton.addActionListener(e -> checkout());
        JButton adminButton = new JButton("관리자 모드");
        adminButton.addActionListener(e -> adminMode());

        // 하단 버튼 패널 - 결제와 관리자 모드 버튼
        JPanel bottomButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomButtons.add(checkoutButton);
        bottomButtons.add(adminButton);
        buttonPanel.add(bottomButtons, BorderLayout.SOUTH);

        // 메인 프레임에 컴포넌트 배치 - BorderLayout 사용
        add(categoryPanel, BorderLayout.NORTH);
        add(menuScrollPane, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.EAST);

        // 초기 화면은 '유니폼' 카테고리로 설정
        showMenuByCategory("유니폼");
        setVisible(true);
    }

    // 장바구니 업데이트 메서드 - 장바구니 내용 및 총 금액 갱신
    private void updateCart() {
        cartAreaPanel.removeAll();  // 기존 장바구니 항목을 제거

        int total = 0;
        for (MenuItem item : cart) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // // 아이템 정보 표시 - 이름, 수량, 가격
            JLabel itemLabel = new JLabel(item.getName() + " x" + item.getQuantity() + " - " + (item.getPrice() * item.getQuantity()) + "원");
            itemPanel.add(itemLabel);

            // 삭제 버튼 추가 - 장바구니에서 해당 아이템 제거
            JButton deleteButton = new JButton("삭제");
            deleteButton.addActionListener(e -> removeFromCart(item));  // 삭제 버튼 클릭 시 해당 아이템 삭제
            itemPanel.add(deleteButton);

            cartAreaPanel.add(itemPanel);  // 장바구니 항목을 추가

            // 총 금액 계산
            total += item.getPrice() * item.getQuantity();
        }

        // 장바구니 내용 및 총 금액 업데이트
        totalLabel.setText("총 금액: " + total + "원");

        cartAreaPanel.revalidate();  // 화면 갱신
        cartAreaPanel.repaint();
    }

    // 장바구니에서 아이템 제거 메서드
    private void removeFromCart(MenuItem item) {
        // 수량이 1보다 많으면 수량 감소
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cart.remove(item);  // 수량이 1이면 장바구니에서 완전히 제거
        }

        updateCart();   // 장바구니 화면 업데이트
    }

 // 결제 처리 메서드
    private void checkout() {
    	// 장바구니 총 금액 계산
        int total = cart.stream().mapToInt(item -> item.getPrice() * item.getQuantity()).sum();
        
        // 결제 확인 대화상자
        int option = JOptionPane.showConfirmDialog(this, "총 금액: " + total + "원\n결제하시겠습니까?", "결제", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            cart.clear(); // 장바구니 비우기
            updateCart(); // 장바구니 화면 업데이트
            JOptionPane.showMessageDialog(this, "결제가 완료되었습니다.");
        }
    }

    // 관리자 모드 메서드 - 메뉴 관리 기능
    private void adminMode() {
    	// 로그인 되어있지 않다면 비밀번호 확인
        if (!isAdminLoggedIn) {
            String password = JOptionPane.showInputDialog(this, "비밀번호를 입력하세요:");
            if ("0000".equals(password)) {
                isAdminLoggedIn = true;
                JOptionPane.showMessageDialog(this, "관리자 로그인 성공!");
            } else {
                JOptionPane.showMessageDialog(this, "비밀번호가 틀렸습니다.");
                return;
            }
        }
        
        // 관리자 기능 선택 대화상자
        String[] adminOptions = {"메뉴 추가", "메뉴 수정", "메뉴 삭제"};
        String choice = (String) JOptionPane.showInputDialog(this, "관리자 모드입니다.", "관리자 메뉴", JOptionPane.QUESTION_MESSAGE, null, adminOptions, adminOptions[0]);
        if (choice != null) {
            switch (choice) {
                case "메뉴 추가" -> addMenuItem();
                case "메뉴 수정" -> modifyMenuItem();
                case "메뉴 삭제" -> deleteMenuItem();
            }
        }
    }

    // 메뉴 추가 메서드 - 새로운 상품 메뉴 생성
    private void addMenuItem() {
    	// 사용자로부터 새 메뉴 정보 입력 받기
        String name = JOptionPane.showInputDialog(this, "메뉴 이름을 입력하세요:");
        if (name == null) return;
        
        // 가격 입력 및 유효성 검사
        String priceString = JOptionPane.showInputDialog
        		(this, "가격을 입력하세요:");
        if (priceString == null) return;
        int price;
        try {
            price = Integer.parseInt(priceString);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "가격을 올바르게 입력하세요.");
            return;
        }

        // 카테고리 및 이미지 경로 입력
        String category = JOptionPane.showInputDialog(this, "카테고리를 입력하세요:");
        if (category == null) return;

        String imagePath = JOptionPane.showInputDialog(this, "이미지 경로를 입력하세요:");
        if (imagePath == null) return;

        // 새 메뉴 아이템 생성 및 메뉴 리스트에 추가
        MenuItem newItem = new MenuItem(name, price, category, imagePath);
        menuList.add(newItem);
        showMenuByCategory(category);
        JOptionPane.showMessageDialog(this, "새로운 메뉴가 추가되었습니다.");
    }

    // 메뉴 수정 메서드 - 기존 상품 정보 변경
    private void modifyMenuItem() {
    	// 기존 메뉴 목록에서 수정할 메뉴 선택
        String[] menuNames = menuList.stream().map(MenuItem::getName).toArray(String[]::new);
        String menuName = (String) JOptionPane.showInputDialog(this, "수정할 메뉴를 선택하세요:", "메뉴 수정", JOptionPane.QUESTION_MESSAGE, null, menuNames, menuNames[0]);

        if (menuName == null) return;

        // 선택된 메뉴 아이템 찾기
        MenuItem itemToModify = menuList.stream().filter(item -> item.getName().equals(menuName)).findFirst().orElse(null);
        if (itemToModify == null) {
            JOptionPane.showMessageDialog(this, "메뉴를 찾을 수 없습니다.");
            return;
        }

        // 새로운 정보 입력 받기 (기존 값 유지 옵션)
        // 가격, 카테고리, 이미지 경로도 동일한 방식으로 수정
        String newName = JOptionPane.showInputDialog(this, "새 메뉴 이름을 입력하세요:", itemToModify.getName());
        if (newName != null && !newName.isEmpty()) {
            itemToModify.setName(newName);
        }

        String newPriceString = JOptionPane.showInputDialog(this, "새 가격을 입력하세요:", itemToModify.getPrice());
        if (newPriceString != null && !newPriceString.isEmpty()) {
            try {
                int newPrice = Integer.parseInt(newPriceString);
                itemToModify.setPrice(newPrice);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "가격을 올바르게 입력하세요.");
            }
        }

        String newCategory = JOptionPane.showInputDialog(this, "새 카테고리를 입력하세요:", itemToModify.getCategory());
        if (newCategory != null && !newCategory.isEmpty()) {
            itemToModify.setCategory(newCategory);
        }

        String newImagePath = JOptionPane.showInputDialog(this, "새 이미지 경로를 입력하세요:", itemToModify.getImagePath());
        if (newImagePath != null && !newImagePath.isEmpty()) {
            itemToModify.setImagePath(newImagePath);
        }

        // 수정된 카테고리의 메뉴 다시 표시
        showMenuByCategory(itemToModify.getCategory());
        JOptionPane.showMessageDialog(this, "메뉴가 수정되었습니다.");
    }

    // 메뉴 삭제 메서드 - 기존 상품 제거
    private void deleteMenuItem() {
    	// 삭제할 메뉴 선택
    	// 메뉴 리스트에서 각 메뉴의 이름을 추출해 배열로 만듭니다.
        String[] menuNames = menuList.stream().map(MenuItem::getName).toArray(String[]::new);
        // 사용자에게 삭제할 메뉴를 선택하도록 대화 상자를 표시합니다.
        String menuName = (String) JOptionPane.showInputDialog(this, "삭제할 메뉴를 선택하세요:", "메뉴 삭제", JOptionPane.QUESTION_MESSAGE, null, menuNames, menuNames[0]);

        // 사용자가 취소를 누른 경우 메서드를 종료합니다.
        if (menuName == null) return;

        // 선택된 메뉴 이름과 일치하는 MenuItem 객체를 찾습니다.
        MenuItem itemToDelete = menuList.stream().filter(item -> item.getName().equals(menuName)).findFirst().orElse(null);
        if (itemToDelete == null) {
        	// 해당 메뉴가 없으면 오류 메시지를 표시하고 메서드를 종료합니다.
            JOptionPane.showMessageDialog(this, "메뉴를 찾을 수 없습니다.");
            return;
        }

        // 삭제 확인 대화 상자를 표시합니다.
        int option = JOptionPane.showConfirmDialog(this, "정말로 이 메뉴를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
        	// 사용자가 "예"를 선택한 경우 메뉴를 삭제합니다.
            menuList.remove(itemToDelete);
            // 삭제된 메뉴와 동일한 카테고리의 메뉴를 새로 갱신하여 화면에 표시합니다.
            showMenuByCategory(itemToDelete.getCategory());
            // 삭제 완료 메시지를 표시합니다.
            JOptionPane.showMessageDialog(this, "메뉴가 삭제되었습니다.");
        }
    }

    private void showMenuByCategory(String category) {
    	// 삭제 완료 메시지를 표시합니다.
        menuPanel.removeAll();  

        // 메뉴 리스트를 순회하며 주어진 카테고리에 해당하는 메뉴만 화면에 표시합니다.
        for (MenuItem item : menuList) {
            if (item.getCategory().equals(category)) {
                // 개별 메뉴 아이템을 표시하기 위한 패널 생성
                JPanel itemPanel = new JPanel();
                itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));  // 수직으로 배치

                // 이미지 표시
                ImageIcon icon = new ImageIcon(item.getImagePath());
                JLabel imageLabel = new JLabel(icon);
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // 이미지 중앙 정렬
                itemPanel.add(imageLabel);  // 이미지 추가

                // 제품 이름 표시
                JLabel nameLabel = new JLabel(item.getName(), SwingConstants.CENTER);
                nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // 이름 중앙 정렬
                itemPanel.add(nameLabel);  // 이름 추가

                // 가격 표시
                JLabel priceLabel = new JLabel(item.getPrice() + "원", SwingConstants.CENTER);
                priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  // 가격 중앙 정렬
                itemPanel.add(priceLabel);  // 가격 추가

                // 추가 버튼
                JButton addButton = new JButton("추가");
                addButton.setAlignmentX(Component.CENTER_ALIGNMENT);  // 버튼 중앙 정렬
                // 버튼 클릭 시 해당 아이템을 장바구니에 추가하는 동작을 정의합니다.
                addButton.addActionListener(e -> addToCart(item));
                itemPanel.add(addButton);  // 추가 버튼 추가

                // 메뉴 패널에 생성된 아이템 패널을 추가합니다.
                menuPanel.add(itemPanel);
            }
        }

        // 메뉴 패널을 갱신하여 변경 사항을 반영합니다.
        menuPanel.revalidate();
        menuPanel.repaint();
    }



    private void addToCart(MenuItem item) {
    	// 장바구니에 동일한 아이템이 이미 있는지 확인합니다.
        boolean alreadyInCart = false;
        for (MenuItem cartItem : cart) {
            if (cartItem.getName().equals(item.getName())) {
            	// 동일한 아이템이 있으면 수량을 증가시킵니다.
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                alreadyInCart = true;
                break;
            }
        }

        // 장바구니에 동일한 아이템이 없으면 새로 추가합니다.
        if (!alreadyInCart) {
            cart.add(item);
        }

     // 장바구니를 갱신하여 변경 사항을 반영합니다.
        updateCart();  // 장바구니 업데이트
    }
}

