package kr.co.ex.hiwaysnsclient.lib;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class TrOasisXmlProc
{
	/*
	 * Constant 정의.
	 */
	public	static	final	String	XML_LEADING	= "<?xml version='1.0' encoding='UTF-8'?>";

	
	/*
	 * Class 및 Instance Variable 정의.
	 */
	//XML 파일의 내용.
	private	String	mXmlData	= "";
	

	/*
	 * Method 정의.
	 */
	//XML 데이터 생성 시작.
	public	void	startXML()
	{
		mXmlData	= XML_LEADING;
	}

	//XML에 신규 필드 추가.
	public	String	appendField( String fieldName, String fieldValue )
	{
		mXmlData	= mXmlData + "<" + fieldName + ">" + fieldValue + "</" + fieldName + ">";
		return mXmlData;
	}

	public	String	appendField_Int( String fieldName, int fieldValue )
	{
		String	strValue	= String.valueOf(fieldValue);
		return appendField( fieldName, strValue );
	}

	public	String	appendField_Long( String fieldName, long fieldValue )
	{
		String	strValue	= String.valueOf(fieldValue);
		return appendField( fieldName, strValue );
	}

	//XML에 신규 필드 추가를 위한 작업시작.
	public	String	startField( String fieldName )
	{
		mXmlData	= mXmlData + "<" + fieldName + ">";
		return mXmlData;
	}

	//XML에 신규 필드 추가를 위한 작업완료.
	public	String	endField( String fieldName )
	{
		mXmlData	= mXmlData + "</" + fieldName + ">";
		return mXmlData;
	}
	
	//XML 데이터 생성 완료.
	public	String	endXML()
	{
		return mXmlData;
	}

	
	
	//GET 또는 POST 방식으로 수신된 XML 입력정보 파싱.
	public	String[][]	parseInputXML( String strInputXml, String[][] inputList )
	{
		try
		{
			//입력인자 목록의 데이터 초기화.
			for ( int j = 0; j < inputList.length; j++ )	inputList[j][1] = "";

			//입력 XML 문서에서 입력인자의 데이터 추출 및 저장.
			/*
			DocumentBuilderFactory	factory	= DocumentBuilderFactory.newInstance();
			DocumentBuilder			builder	= factory.newDocumentBuilder();
			
			InputSource	is	= new InputSource( new StringReader(strInputXml) );
			Document	doc	= builder.parse( is );
			NodeList	troasis	= doc.getElementsByTagName("troasis");
			NodeList	channel	= troasis.item(0).getChildNodes();
			int		countList	= (int)channel.getLength();
			String	tagName;
			for ( int i = 0; i < countList; i++ )
			{
				tagName		= channel.item(i).getNodeName();
				if ( tagName.length() < 1 || tagName.getBytes()[0] == '#' )	continue;
				for ( int j = 0; j < inputList.length; j++ )
				{
					if ( tagName.compareToIgnoreCase(inputList[j][0]) == 0 )
						inputList[j][1] = channel.item(i).getNodeValue();
				}
			}
			*/
			XmlPullParserFactory	factory	= XmlPullParserFactory.newInstance(); 
			factory.setNamespaceAware( true ); 
			XmlPullParser			xpp		= factory.newPullParser(); 
			//InputSource	is	= new InputSource( new StringReader(strInputXml) );
			ByteArrayInputStream is	= new ByteArrayInputStream(strInputXml.toString().getBytes("UTF-8"));
			xpp.setInput( is, "utf-8" ); 
			int			eventType	= xpp.getEventType(); 
			String		tagName;
			String		strValue;
			//Log.i( "[ANDROID] ",strInputXml ); 
			while ( eventType != XmlPullParser.END_DOCUMENT )
			{
				tagName	= xpp.getName();
				switch( eventType )
				{
				case XmlPullParser.START_DOCUMENT	:
					//Log.i( "[ANDROID] ", "Start document");
					break;
					
				case XmlPullParser.END_DOCUMENT		:
					//Log.i( "[ANDROID] ", "End document");
					break;
					
				case XmlPullParser.START_TAG		:
					//Log.i( "[ANDROID] ", "Start tag " + tagName);
					if ( tagName.length() < 1 || tagName.getBytes()[0] == '#' )
					{
						eventType = xpp.next(); 
						continue;
					}
					//Log.i( "[ANDROID] ","Start tag :" + tagName + " : " + xpp.nextToken() + "," + xpp.getText() );
					//Log.i( "[ANDROID] ","Start tag :" + tagName + " : " + "," + xpp.getText() );
					for ( int j = 0; j < inputList.length; j++ )
					{
						if ( tagName.equalsIgnoreCase(inputList[j][0]) )
						{
							xpp.nextToken();
							strValue	= xpp.getText();
	 							if ( strValue == null || strValue.startsWith("</") )	strValue = "";
	 							inputList[j][1]	= strValue;
							//Log.i( "[FIELD] ","Field=" + tagName + ", Value=" + strValue ); 
						}
					}
					break;
					
				case XmlPullParser.END_TAG	:
					//Log.i( "[ANDROID] ","End tag : " + tagName );
					break;
					
				case XmlPullParser.TEXT		:
					//Log.i( "[ANDROID] ","Text : " + tagName + " : " + xpp.getText() );
					break;
					
				default						:
					//Log.i( "[ANDROID] ","Else : " + tagName + " With : " + eventType );
					break;
				}
					
				eventType = xpp.next(); 
			}
		}
		catch( Exception e )
		{
		}
		
		//입력정보가 저장된 목록 반환.
		return inputList;
	}
	
	//GET 또는 POST 방식으로 수신된 XML 입력정보 파싱.
	public	List<String[]>	parseMemberXML( String strInputXml, String subMember, String[] listMember )
	{
		//입력인자 목록의 데이터 초기화.
		List<String[]>	listMemberValue	= new ArrayList<String[]>();

		try
		{
			//입력 XML 문서에서 입력인자의 데이터 추출 및 저장.
			XmlPullParserFactory	factory	= XmlPullParserFactory.newInstance(); 
			factory.setNamespaceAware( true ); 
			XmlPullParser			xpp		= factory.newPullParser(); 
			//InputSource	is	= new InputSource( new StringReader(strInputXml) );
			ByteArrayInputStream is	= new ByteArrayInputStream(strInputXml.toString().getBytes("UTF-8"));
			xpp.setInput( is, "utf-8" ); 
			int			eventType	= xpp.getEventType(); 
			String		tagName;
			int			index	= -1;
			String		strValue;
			//Log.i( "[ANDROID] ",strInputXml ); 
			while ( eventType != XmlPullParser.END_DOCUMENT )
			{
				tagName	= xpp.getName();
				switch( eventType )
				{
				case XmlPullParser.START_DOCUMENT	:
					//Log.i( "[ANDROID] ", "Start document");
					break;
					
				case XmlPullParser.END_DOCUMENT		:
					//Log.i( "[ANDROID] ", "End document");
					break;
					
				case XmlPullParser.START_TAG		:
					//Log.i( "[ANDROID] ", "Start tag " + tagName);
					if ( tagName.length() < 1 || tagName.getBytes()[0] == '#' )
					{
						eventType = xpp.next(); 
						continue;
					}
					//Log.i( "[ANDROID] ","Start tag :" + tagName + " : " + xpp.nextToken() + "," + xpp.getText() );
					//Log.i( "[ANDROID] ","Start tag :" + tagName + " : " + "," + xpp.getText() );
					//Log.i( "[ANDROID] ","Start tag=" + tagName + " : subMember=" + subMember );
					//결과항목 추가.
					if ( tagName.equalsIgnoreCase(subMember) )
					{
						listMemberValue.add( new String[listMember.length] );
						index++;
							//Log.i( "[MEMBER] ","index :" + index ); 
					}
					//결과항목 추출.
					for ( int j = 0; j < listMember.length; j++ )
					{
						if ( tagName.equalsIgnoreCase(listMember[j]) )
						{
							xpp.nextToken();
							strValue	= xpp.getText();
	 							if ( strValue == null || strValue.startsWith("</") )	strValue = "";
							listMemberValue.get(index)[j]	= strValue;
							//Log.i( "[FIELD] ","Field=" + tagName + ", Value=" + strValue ); 
						}
					}
					break;
					
				case XmlPullParser.END_TAG	:
					//Log.i( "[ANDROID] ","End tag : " + tagName );
					break;
					
				case XmlPullParser.TEXT		:
					//Log.i( "[ANDROID] ","Text : " + tagName + " : " + xpp.getText() );
					break;
					
				default						:
					//Log.i( "[ANDROID] ","Else : " + tagName + " With : " + eventType );
					break;
				}
					
				eventType = xpp.next(); 
			}
		}
		catch( Exception e )
		{
		}
		
		//입력정보가 저장된 목록 반환.
		return listMemberValue;
	}
	
	
	/*
	 * Attribute 정의.
	 */
	public	String	getXmlData()
	{
		return mXmlData;
	}

	
	/*
	 * Implementation 정의.
	 */
}
