/*
 * 
 *	Hi-Way SNS 서버.
 *
 * Copyrights (c) 2010-2011, (주)맨크레드. All rights reserved.
 *
 * 저작자: 유석(Yoo, Seok)
 *
 */

package kr.co.ex.hiwaysnsclient.lib;

public class TrOasisParamPassing
{
	/*
	 * Constant 정의.
	 */

	
	/*
	 * Class 및 Instance 변수 정의.
	 */
	
	
	/*
	 * 객체 생성자.
	 */
	public	TrOasisParamPassing()
	{
	}
	
	
	/*
	 * 입력인자 처리.
	 */
	//GET 또는 POST 방식으로 전달되는 입력인자 수신하기.
	public	String	get_input_param( String strParam )
	{
		try
		{
			strParam	= new String( strParam.getBytes("8859_1"), "utf-8" );
		}
		catch( Exception e ) { }
		
		return strParam;
	}
	
	/*
	 * Utilities.
	 */
	//DB에서 작업이 수행된 시각의 Timestamp.
	public	long	getCurrentTimestamp()
	{	
		//1970년 1월 1일 0시를 기준으로 1초 단위의 값을 사용한다.
       	return( System.currentTimeMillis() / 1000 );
	}
}

/*
 * End of File.
 */