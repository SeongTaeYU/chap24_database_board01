package com.javalab.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class board01 {

	//1. Oracle 드라이버 경로 문자열 상수
	public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

	//2. Oracle 데이터베이스 접속 경로(url) 문자열 상수
	public static final String DB_URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	
	//3. 데이터베이스 접속 객체
	public static Connection con = null;
	
	//4. query 실행 객체
	public static PreparedStatement pstmt = null;
	
	//5. select 결과 저장 객체
	public static ResultSet rs = null;
	
	//6. Oracle 계정
	public static String oracleId = "board";
	
	//7. Oracle 계정 패스워드
	public static String oraclePwd = "1234";
	
	
	public static void main(String[] args) {
		//1. 디비 접속 메소드 호출
		connectDB();
		
		//2.게시물 목록 조회
		getBoardList();
		
		//3. 새글 등록
		// 새글 등록이 완료됭었으면 주석처 한 후에 답글 등록으로 이동
		insertNewBoard();
		
		//4. 답글 등록
		// 어떤 게시물에 답글을 달지 무모 게시글의 정보를 전달해야함
		int replyGroup = 17;	// 부모글의 그룹번호
		int replyOrder = 1;		// 부모글의 그룹내 순서
		int replyIndent =1;		// 부모글의 들여쓰기
		
		insertReply(replyGroup, replyOrder, replyIndent);
		
		// 5. 게시물 목록 조회(반드시 1번 ~ 5번까지)
		int startNo = 1;
		int length = 5;
		getBoardListTopN(startNo, length);
		
		// 6. 중간에 특정 부분 조회(5~9번까지)
		startNo = 5;
		length = 9;
		
		getBoardListPart(startNo, length);
		
		// 7. 게시물 조회수 증가
		int bno = 2; //조회수를 증가시킬 게시물 번호
		updateCount(bno);
		
		//8. 수정
		//5번 게시물의 제목을 "다섯번째 글"로 수정하세요.
		bno = 5;
		String newTitle = "다섯번째 글";
		updateTitle(bno, newTitle);
		
		//9.user01님이 작성한 게시물을 모두 삭제하세요
		bno = 6;	//삭제할 게시물 번호
		deleteBoard(bno);
		
		//rs, pstmt 자원반납
		closeResource(rs,pstmt);
		
		//con 자원반납
		closeResource();
	}//end main
	
	//9.user01님이 작성한 게시물을 모두 삭제하세요
	private static void deleteBoard(int bno) {
		try {
			String memberId = "user01";
			String sql = "delete tbl_board ";
				   sql += " where bno =? and member_id = ?";
			   pstmt = con.prepareStatement(sql);
			   pstmt.setInt(1, bno);
			   pstmt.setString(2,memberId);
			   
			   int result = pstmt.executeUpdate();
			   
			   if(result>0) {
				   System.out.println("9. 데이터 삭제 성공");
			   }else {
				   System.out.println("9. 데이터 삭제 실패");
			   }
			   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end deleteBoard(int bno)


//	8. 수정 - 5번 게시물의 제목을 "다섯번째 글"로 수정하세요.

	private static void updateTitle(int bno, String newTitle) {
		try {
			String sql = "update tbl_board";
				   sql += " set title = ? where bno = ?";
				   
				   pstmt = con.prepareStatement(sql);
				   pstmt.setString(1, newTitle);
				   pstmt.setInt(2,bno);
				   
				   int result = pstmt.executeUpdate();
				   
				   if(result>0) {
					   System.out.println("8. 5번 게시물의 제목을 \"다섯번째 글\"로 수정 성공");
				   }else {
					   System.out.println("8. 5번 게시물의 제목을 \"다섯번째 글\"로 수정 실패");
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
		
	}


//	 7. 게시물 조회수 증가
	private static void updateCount(int bno) {
		try {
			String sql = "update tbl_board";
				   sql += " set count = count+1 where bno = ?";
				   
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1, bno);
				   
				   int result = pstmt.executeUpdate();
				   
				   if(result>0) {
					   System.out.println("7. 게시물 조회수 증가 수정 성공");
				   }else {
					   System.out.println("7. 게시물 조회수 증가 수정 실패");
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
		
	}//end updateCount(int countBno)


//	 6. 중간에 특정 부분 조회(5~9번까지)
	private static void getBoardListPart(int startNo, int length) {
		try {
			String sql = "select b.*";
				   sql += " from(";
				   sql += "      select rownum rnum , a.*";
				   sql += "        from( ";
				   sql += "             select b.bno, b.title, b.content, b.member_id, b.count, to_char(b.created_date,'yyyy-mm-dd') creeated_date, b.reply_group, b.reply_order, b.reply_indent";
				   sql += "             from tbl_board b";
				   sql += "             order by created_date desc";
				   sql += "         )a";
				   sql += "     )b";
				   sql += "     where rnum between ? and ?";
				   
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1, startNo);
				   pstmt.setInt(2,length);
				   
				   rs = pstmt.executeQuery();
				   System.out.println("6. 중간에 특정 부분 조회(5~9번까지)");
				   
				   while(rs.next()){
					   String strInd = "";
					   int indent = rs.getInt("reply_indent");
					   if(indent >0) {
						   for (int i = 0; i < indent; i++) {
							strInd += " ";
						   }
					   }
					   System.out.println(strInd+
							   			  rs.getInt("bno")+"\t"+
							   			  rs.getString("title")+"\t"+
							   			  rs.getString("content")+"\t"+
							   			  rs.getString("member_id")+"\t"+
							   			  rs.getInt("count")+"\t"+
							   			  rs.getString("creeated_date")+"\t"+
							   			  rs.getInt("reply_group")+"\t"+
							   			  rs.getInt("reply_order")+"\t"+
							   			  rs.getInt("reply_indent")
							   );
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end getBoardListPart(int startNo, int length)

//	5. 게시물 목록 조회(반드시 1번 ~ 5번까지)
	private static void getBoardListTopN(int startNo, int length) {
		try {
			String sql = "select b.*";
				   sql += " from(";
				   sql += "      select rownum rnum , a.*";
				   sql += "        from( ";
				   sql += "             select b.bno, b.title, b.content, b.member_id, b.count, to_char(b.created_date,'yyyy-mm-dd') creeated_date, b.reply_group, b.reply_order, b.reply_indent";
				   sql += "             from tbl_board b";
				   sql += "             order by created_date desc";
				   sql += "         )a";
				   sql += "     )b";
				   sql += "     where rnum between ? and ?";
				   
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1, startNo);
				   pstmt.setInt(2, length);
				   
				   rs = pstmt.executeQuery();
				   System.out.println("5. 게시물 목록 조회(반드시 1번 ~ 5번까지)");
				   
				   while(rs.next()){
					   String strInd = "";
					   int indent = rs.getInt("reply_indent");
					   if(indent >0) {
						   for (int i = 0; i < indent; i++) {
							strInd += " ";
						   }
					   }
					   System.out.println(strInd+
							   			  rs.getInt("bno")+"\t"+
							   			  rs.getString("title")+"\t"+
							   			  rs.getString("content")+"\t"+
							   			  rs.getString("member_id")+"\t"+
							   			  rs.getInt("count")+"\t"+
							   			  rs.getString("creeated_date")+"\t"+
							   			  rs.getInt("reply_group")+"\t"+
							   			  rs.getInt("reply_order")+"\t"+
							   			  rs.getInt("reply_indent")
							   );
				   }
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end getBoardListTopN(int startNo, int length) 


//	4. 답글 등록
	private static void insertReply(int replyGroup, int replyOrder, int replyIndent) {
		try {
			String sql = "insert into tbl_board(bno, title, content , member_id, count,created_date, reply_group, reply_order, reply_indent)";
				   sql += " values(seq_bno.nextval, '[새글의 답글] 데이터베이스 내용 추가','[새글의 답글] 데이터베이 내용 추가 데이터베이스란?','user01',0,to_date('22/04/12','RR/MM/DD'),?,?,?)";
				   
				   pstmt = con.prepareStatement(sql);
				   pstmt.setInt(1,replyGroup);
				   pstmt.setInt(2,replyOrder);
				   pstmt.setInt(3,replyIndent);
				   
				   int result = pstmt.executeUpdate();
				   
				   if(result > 0) {
					   System.out.println("4. 답글 등록 저장 성공");
				   }else {
					   System.out.println("4. 답글 등록 저장 실패");
				   }
				   
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR"+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end insertReply(int replyGroup, int replyOrder, int replyIndent)



//	3. 새글 등록
	private static void insertNewBoard() {
		try {
			String sql = "insert into tbl_board(bno, title, content , member_id, count,created_date, reply_group, reply_order, reply_indent)";
				   sql += "  values(seq_bno.nextval, '[새글] 데이터베이스 내용 추가','[새글] 데이터베이 내용 추가 데이터베이스란?','user01',0,to_date('22/04/12','RR/MM/DD'),seq_bno.currval,0,0)";
			
				   pstmt = con.prepareStatement(sql);
				   System.out.println("");
				   int result = pstmt.executeUpdate();
				   
				   if(result >0) {
					   System.out.println("3. 데이터 삽입 저장 성공");
				   }else {
					   System.out.println("3. 데이터 삽입 저장 실패");
				   }
				  
				   System.out.println();
		}catch(SQLException e) {
			System.out.println("SQL ERR :"+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end insertNewBoard()


	//2.게시물 목록 조회
	private static void getBoardList() {
		try {
			String sql = "select b.bno, b.title, b.content, b.member_id, b.count, to_date(b.created_date,'yyyy-mm-dd') created_date, b.reply_group, b.reply_order, b.reply_indent,";
				   sql += " m.name, m.pwd, m.email, m.handphone, m.admin, m.address";
				   sql += " from tbl_board b left outer join tbl_member m on b.member_id = m.member_id";
				   
			pstmt = con.prepareStatement(sql);
			System.out.println("2. 게시물 목록 전체 조회");
			System.out.println();
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String strInd = "";
				   int indent = rs.getInt("reply_indent");
				   if(indent >0) {
					   for (int i = 0; i < indent; i++) {
						strInd += " ";
					   }
				   }
				System.out.println( strInd+
								    rs.getInt("bno")+"\t"+
									rs.getString("title")+"\t"+
									rs.getString("content")+"\t"+
									rs.getString("member_id")+"\t"+
									rs.getInt("count")+"\t"+
									rs.getDate("created_date")+"\t"+
									rs.getInt("reply_group")+"\t"+
									rs.getInt("reply_order")+"\t"+
									rs.getInt("reply_indent")+"\t"+
									rs.getString("name")+"\t"+
									rs.getString("pwd")+"\t"+
									rs.getString("email")+"\t"+
									rs.getString("handphone")+"\t"+
									rs.getInt("admin")+"\t"+
									rs.getString("address")
						);
			}
			
			
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}finally {
			closeResource(rs, pstmt);
		}
	}//end getBoardList()
	

	//1. 디비 접속 메소드 호출
	private static void connectDB() {
		try {
		Class.forName(DRIVER_NAME);
		System.out.println("1-1 드라이버 연결 성공");
		System.out.println();
		
		con = DriverManager.getConnection(DB_URL,oracleId,oraclePwd);
		System.out.println("1-2 커넥션 객체 생성 성공");
		System.out.println();
		
		}catch(ClassNotFoundException e) {
			
			System.out.println("드라이버 ERR : "+e.getMessage());
		}catch(SQLException e) {
			System.out.println("SQL ERR : "+e.getMessage());
		}

	}//end connectDB()
	
	//rs, pstmt 자원반납
	private static void closeResource(ResultSet rs, PreparedStatement pstmt) {
		try {
			if(rs != null) {
				rs.close();
			}
			if(pstmt != null) {
				pstmt.close();
			}
		}catch(SQLException e) {
			System.out.println("자원반납 ERR : "+e.getMessage());
		}
	}//end closeResource(ResultSet rs, PreparedStatement pstmt)

	//con 자원반납
	private static void closeResource() {
		try {
			if(con != null) {
				con.close();
			}
		}catch(SQLException e) {
			System.out.println("자원반납 ERR : "+e.getMessage());
		}
	}//end closeResource()

}//end class board01
