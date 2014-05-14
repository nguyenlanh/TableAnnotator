/*
 * @author: Nikola Milosevic
 * @affiliation: University of Manchester, School of Computer science
 * 
 */
package readers;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import classifiers.SimpleTableClassifier;
import stats.Statistics;
import tablInEx.*;


/**
 * PMCXMLReader class is used to read and parse XML data from PubMed Central database
 * The class takes as input folder with XML documents extracted from PMC database and creates array of Articles {@link Article} as output
 * @author Nikola Milosevic
 */ 
public class PMCXMLReader implements Reader{

	private String FileName;
	
	
	public void init(String file_name)
	{
		setFileName(file_name);
	}
	
	/**
	 * This method is the main method for reading PMC XML files. It uses {@link #ParseMetaData} and {@link #ParseTables} methods.
	 * It returns {@link Article} object that contains structured data from article, including tables.
	 * @return Article
	 */
	public Article Read()
	{
		Article art =  new Article(FileName);
		//TODO: Reading of file
		try{
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(FileName));
		String line = null;
		String xml = "";
		while ((line = reader.readLine()) != null) {
			if(line.contains("JATS-archivearticle1.dtd")||line.contains("archivearticle.dtd"))
				continue;
		    xml +=line+'\n';
		}		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(false);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    InputSource is = new InputSource(new StringReader(xml));
	    Document parse =  builder.parse(is);
	    art = ParseMetaData(art, parse, xml);
		art = ParseTables(art,parse);
		
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return art;
	}
	
	public String getFileName() {
		return FileName;
	}
	public void setFileName(String fileName) {
		FileName = fileName;
	}

	public String[] GetAuthors(Document parse)
	{
		NodeList authors = parse.getElementsByTagName("contrib");
	    String[] auths = new String[authors.getLength()];
	    for(int j = 0; j<authors.getLength(); j++)
	    {
	    	String givenName = "";
	    	String surname = "";
	    	NodeList name = authors.item(j).getChildNodes().item(0).getChildNodes();
	    	if(name.item(1)!=null)
	    		surname = name.item(0).getTextContent();
	    	if(name.item(1)!=null)
	    		givenName = name.item(1).getTextContent();
	    	auths[j] = surname+ ", "+givenName;
	    }
	    return auths;
	}
	
	/**
	 * Gets the affiliations of authors.
	 *
	 * @param parse the parse
	 * @return the string[]
	 */
	public String[] GetAffiliations(Document parse)
	{
	    NodeList affis = parse.getElementsByTagName("aff");
	    String[] affilis = new String[affis.getLength()];
	    for(int j = 0; j<affis.getLength(); j++)
	    {
	    	String affiliation = affis.item(j).getTextContent();
	    	affilis[j] = affiliation;
	    	System.out.println("Affiliation:"+affiliation);
	    }
	    return affilis;
	}
	
	/**
	 * Gets the article keywords.
	 *
	 * @param parse the parse
	 * @return the keywords
	 */
	public String[] getKeywords(Document parse)
	{
	    NodeList keywords = parse.getElementsByTagName("kwd");
	    String[] keywords_str = new String[keywords.getLength()];
	    for(int j = 0; j<keywords.getLength(); j++)
	    {
	    	String Keyword = keywords.item(j).getTextContent().substring(1);
	    	keywords_str[j] = Keyword;
	    	System.out.println("Keyword:"+Keyword);
	    }
	    return keywords_str;
	}
	
	/**
	 * Reads metadata from article such as title, authors, publication type etc
	 * @param art - Article where to put data
	 * @param parse - Document of XML 
	 * @param xml - XML code
	 * @return Article - populated art
	 */
	public Article ParseMetaData(Article art, Document parse, String xml)
	{
	    String title = parse.getElementsByTagName("article-title").item(0).getTextContent();
	    title = title.replaceAll("\n", "");
	    title = title.replaceAll("\t", "");
	    System.out.println(title);
	    String[] auths = GetAuthors(parse);
	    for(int j = 0; j<auths.length; j++)
	    {
	    	System.out.println(auths[j]);
	    }

	    NodeList issn = parse.getElementsByTagName("issn");
	    for(int j=0;j<issn.getLength();j++)
	    {
	    	if(issn.item(j).getAttributes().getNamedItem("pub-type").getNodeValue().equals("ppub"))
	    	{
	    	String issnp = issn.item(j).getTextContent();	
	    	art.setPissn(issnp);
	    	if(issnp!=null)
	    		System.out.println(issnp);
	    	}
	    	if(issn.item(j).getAttributes().getNamedItem("pub-type").getNodeValue().equals("epub"))
	    	{
	    		String issne = issn.item(j).getTextContent();	
		    	art.setPissn(issne);
		    	if(issne!=null)
		    		System.out.println(issne);
	    	}
	    }
	    NodeList article_id = parse.getElementsByTagName("article-id");
	    for(int j=0;j<article_id.getLength();j++)
	    {
	    	if(article_id.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("pmid"))
	    	{
	    	String pmid = article_id.item(j).getTextContent();	
	    	art.setPmid(pmid);
	    	if(pmid!=null)
	    		System.out.println(pmid);
	    	}
	    	if(article_id.item(j).getAttributes().getNamedItem("pub-id-type").getNodeValue().equals("pmc"))
	    	{
	    		String pmc = article_id.item(j).getTextContent();	
		    	art.setPmc(pmc);
		    	if(pmc!=null)
		    		System.out.println(pmc);
	    	}
	    }
	    
	    String[] affilis = GetAffiliations(parse);	    
	    art.setAffiliation(affilis);
	    NodeList art_abstract = parse.getElementsByTagName("abstract");
	    for(int j=0;j<art_abstract.getLength();j++)
	    {
	    	if(art_abstract.item(j).getAttributes().getNamedItem("abstract-type")!=null&&art_abstract.item(j).getAttributes().getNamedItem("abstract-type").getNodeValue().equals("short"))
	    	{
	    		art.setShort_abstract(art_abstract.item(j).getTextContent());
	    	}
	    	else
	    	{
	    		art.setAbstract(art_abstract.item(j).getTextContent());
	    	}
	    }
	    
	    String[] keywords_str = getKeywords(parse);
	    art.setKeywords(keywords_str);
	    String publisher_name = "";
	    if(parse.getElementsByTagName("publisher-name").item(0)!=null)
	    	publisher_name = parse.getElementsByTagName("publisher-name").item(0).getTextContent();
	    art.setPublisher_name(publisher_name);
	    if(publisher_name!=null)
	    	System.out.println(publisher_name);
	    String publisher_loc = "";
	    if(parse.getElementsByTagName("publisher-loc").item(0)!=null)
	    publisher_loc = parse.getElementsByTagName("publisher-loc").item(0).getTextContent();
	    art.setPublisher_loc(publisher_loc);
	    if(publisher_loc!=null)
	    	System.out.println(publisher_loc);
	    try{
	    if(parse.getElementsByTagName("body").item(0)!=null)
	    {
	    String plain_text = parse.getElementsByTagName("body").item(0).getTextContent();
	    art.setPlain_text(plain_text);
	    }
	    }
	    catch(Exception ex)
	    {
	    	ex.printStackTrace();
	    }

	    art.setTitle(title);
	    art.setXML(xml);
	    art.setAuthors(auths);
		return art;
	}
	
	/**
	 * Read table label.
	 *
	 * @param tablexmlNode the tablexml node
	 * @return the string
	 */
	public String readTableLabel(Node tablexmlNode)
	{
		String label = "Table without label";
		List<Node> nl = getChildrenByTagName(tablexmlNode,"label");
		if(nl.size()>0)
		{
			label = nl.get(0).getTextContent();
		}
		
		return label;
	}
	
	/**
	 * Read table caption.
	 *
	 * @param tablexmlNode the tablexml node
	 * @return the string
	 */
	public String readTableCaption(Node tablexmlNode)
	{
		String caption = "";
		List<Node>nl = getChildrenByTagName(tablexmlNode,"caption");
		if(nl.size()>0){
			caption = nl.get(0).getTextContent();
		}
		nl = getChildrenByTagName(tablexmlNode,"p");
		if(nl.size()>0){
			caption = nl.get(0).getTextContent();
		}
		nl = getChildrenByTagName(tablexmlNode,"title");
		if(nl.size()>0){
			caption = nl.get(0).getTextContent();
		}
		return caption;
	}
	
	/**
	 * Read table footer.
	 *
	 * @param tablesxmlNode the tablesxml node
	 * @return the string
	 */
	public String ReadTableFooter(Node tablesxmlNode)
	{
		String foot = "";
		List<Node> nl = getChildrenByTagName(tablesxmlNode,"table-wrap-foot");
		if(nl.size()>=1)
		{
			foot = nl.get(0).getTextContent();
		}
		return foot;
	}
	
	/**
	 * Count columns.
	 *
	 * @param rowsbody the rowsbody
	 * @param rowshead the rowshead
	 * @return the int
	 */
	public int CountColumns(List<Node> rowsbody,List<Node> rowshead)
	{
		int cols=0;
		int headrowscount= 0;
		if(rowshead!=null)
			headrowscount = rowshead.size();
		for(int row = 0;row<rowsbody.size();row++)
		{
			int cnt=0;
			List<Node> tds = getChildrenByTagName(rowsbody.get(row), "td");
			for(int k=0;k<tds.size();k++)
			{
				if(tds.get(k).getAttributes().getNamedItem("colspan")!=null && Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue())>1)
				{
					cnt+=Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue());
				}
				else
				{
					cnt++;
				}
			}
			cols = Math.max(cols,cnt);
		}
		if(headrowscount!=0)
		{
			List<Node> tdsh =  getChildrenByTagName(rowshead.get(0), "td");
			if(tdsh.size()==0){
				tdsh =  getChildrenByTagName(rowshead.get(0), "th");
			}
		cols = Math.max(cols, tdsh.size());
		}
		
		return cols;
	}
	
	/**
	 * Process table header.
	 *
	 * @param table the table
	 * @param cells the cells
	 * @param rowshead the rowshead
	 * @param headrowscount the headrowscount
	 * @param num_of_columns the num_of_columns
	 * @return the table
	 */
	public Table ProcessTableHeader(Table table, Cell[][] cells,List<Node> rowshead,int headrowscount,int num_of_columns)
	{
		for(int j = 0;j<headrowscount;j++)
		{
			Statistics.addHeaderRow();
			table.stat.AddHeaderRow();
			List<Node> tds = getChildrenByTagName(rowshead.get(j), "td");
			if(tds.size()==0)
				tds = getChildrenByTagName(rowshead.get(j), "th");
			int index = 0;
			//read cells
			for(int k = 0;k<tds.size();k++)
			{			
				boolean is_colspanning = false;
				boolean is_rowspanning = false;
				int colspanVal = 1;
				int rowspanVal = 1;
				if(tds.get(k).getAttributes().getNamedItem("rowspan")!=null && Utilities.isNumeric(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue()) && Integer.parseInt(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue())>1)
				{
					table.setRowSpanning(true);
					Statistics.addRowSpanningCell();
					table.stat.AddRowSpanningCell();
					is_rowspanning = true;
					rowspanVal =  Integer.parseInt(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue());											
				}
				//colspan
				if(tds.get(k).getAttributes().getNamedItem("colspan")!=null && Utilities.isNumeric(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue()) && Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue())>1)
				{
					table.setColSpanning(true);
					Statistics.addColumnSpanningCell();
					table.stat.AddColSpanningCell();
					is_colspanning = true;
					colspanVal =  Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue());					
				}

				for(int l=0;l<colspanVal;l++)
				{
					int rowindex = j;
					for(int s =0;s<rowspanVal;s++)
					{
						try
						{
							while(cells[rowindex][index].isIs_filled() && index!=num_of_columns)
								index++;
							cells[rowindex][index] = Cell.setCellValues(cells[rowindex][index], tds.get(k).getTextContent(), is_colspanning, colspanVal, is_rowspanning, rowspanVal, true, 1, false, 0, index,rowindex, l, s);
							//System.out.println(j+","+index+": "+cells[j][index].getCell_content());
							table = Statistics.statisticsForCell(table, cells[rowindex][index]);
						}
						catch(Exception ex)
						{
							System.out.println("Error: Table is spanning more then it is possible");
						}
						rowindex++;
					}
					index++;
				}					
			}//end for tds.size()
		}// end for rowheads
		return table;
	}
	
	/**
	 * Process table body.
	 *
	 * @param table the table
	 * @param cells the cells
	 * @param rowsbody the rowsbody
	 * @param headrowscount the headrowscount
	 * @param num_of_columns the num_of_columns
	 * @return the table
	 */
	public Table ProcessTableBody(Table table, Cell[][] cells,List<Node> rowsbody,int headrowscount, int num_of_columns)
	{
		int startj = headrowscount;
		for(int j = 0;j<rowsbody.size();j++)
		{
			table.stat.AddBodyRow();
			List<Node> tds = getChildrenByTagName(rowsbody.get(j), "td");
			int index = 0;
			int rowindex = startj;
			for(int k = 0;k<tds.size();k++)
			{
				boolean isStub = false;
				float stubProbability =0;
				
				if(index ==0)
				{
					isStub = true;
					stubProbability = (float) 0.9;
				}
				
				
				boolean is_colspanning = false;
				boolean is_rowspanning = false;
				int colspanVal = 1;
				int rowspanVal = 1;
				if(tds.get(k).getAttributes().getNamedItem("rowspan")!=null && Utilities.isNumeric(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue()) && Integer.parseInt(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue())>1)
				{
					table.setRowSpanning(true);
					Statistics.addRowSpanningCell();
					table.stat.AddRowSpanningCell();
					is_rowspanning = true;
					rowspanVal =  Integer.parseInt(tds.get(k).getAttributes().getNamedItem("rowspan").getNodeValue());											
				}
				//colspan
				if(tds.get(k).getAttributes().getNamedItem("colspan")!=null && Utilities.isNumeric(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue()) && Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue())>1)
				{
					table.setColSpanning(true);
					Statistics.addColumnSpanningCell();
					table.stat.AddColSpanningCell();
					is_colspanning = true;
					colspanVal =  Integer.parseInt(tds.get(k).getAttributes().getNamedItem("colspan").getNodeValue());					
				}
				for(int l=0;l<colspanVal;l++)
				{
					rowindex = startj+j;
					for(int s =0;s<rowspanVal;s++)
					{
						try
						{
							while(cells[rowindex][index].isIs_filled() && index!=num_of_columns)
								index++;
							cells[rowindex][index] = Cell.setCellValues(cells[rowindex][index], tds.get(k).getTextContent(), is_colspanning, colspanVal, is_rowspanning, rowspanVal, false, 0, isStub, stubProbability, index,rowindex, l, s);
							//System.out.println(j+","+index+": "+cells[j][index].getCell_content());
							table = Statistics.statisticsForCell(table, cells[rowindex][index]);
						}
						catch(Exception ex)
						{
							System.out.println("Error: Table is spanning more then it is possible");
						}
						rowindex++;
					}
					index++;
				}
			}//end for tds.size()
		}// end for rowheads
	//	cells = TableSimplifier.DeleteEmptyRows(cells);
		return table;
	}
	
	public int getNumOfTablesInArticle(NodeList tablesxml)
	{
		int numOfTables = 0;
		for(int i = 0;i<tablesxml.getLength();i++)
		{
			List<Node> tb = getChildrenByTagName(tablesxml.item(i),"table");
			numOfTables+=tb.size();
		}
		if(numOfTables<tablesxml.getLength())
			numOfTables = tablesxml.getLength();
		
		return numOfTables;
	}
	
	
	/**
	 * Parses table, makes matrix of cells and put it into Article object
	 * @param article - Article to populate
	 * @param parse - Document which is being parsed
	 * @return populated Article
	 */
	public Article ParseTables(Article article, Document parse)
	{
		NodeList tablesxml = parse.getElementsByTagName("table-wrap");
		int numOfTables =  getNumOfTablesInArticle(tablesxml);
		
		Table[] tables = new Table[numOfTables];
		article.setTables(tables);
		int tableindex = 0;
		//Iterate document tables
		for(int i = 0;i<tablesxml.getLength();i++)
		{
			//TODO: This should go up, not to count in statistics tables that are stored in images
			List<Node> tb = getChildrenByTagName(tablesxml.item(i),"table");

			for(int s = 0;s<tb.size();s++)
			{
			Statistics.addTable();
			String label = readTableLabel(tablesxml.item(i));
			
			tables[tableindex] = new Table(label);
			tables[tableindex].setDocumentFileName("PMC"+article.getPmc());
			tables[tableindex].setXml(Utilities.CreateXMLStringFromSubNode(tablesxml.item(i)));
			System.out.println("Table title:"+tables[tableindex].getTable_title());
			String caption = readTableCaption(tablesxml.item(i));
			tables[tableindex].setTable_caption(caption);
			String foot = ReadTableFooter(tablesxml.item(i));
			tables[tableindex].setTable_footer(foot);
			System.out.println("Foot: "+foot);

			//count rows
			int headsize = 0;
			List<Node> thead = null;
			if(tb.size()>0){
				thead = getChildrenByTagName(tb.get(s), "thead");
				headsize = thead.size();
			}
			List<Node> rowshead = null;
			if(headsize>0)
			{
				rowshead = getChildrenByTagName(thead.get(0), "tr");
			}
			else
			{
				tables[tableindex].setHasHeader(false);
				Statistics.TableWithoutHead();
			}
			List<Node> tbody = getChildrenByTagName(tb.get(s), "tbody");
			if(tbody.size()==0)
			{
				Statistics.TableWithoutBody();
				tables[tableindex].setHasBody(false);
				tableindex++;
				continue;
			}
			List<Node> rowsbody = getChildrenByTagName(tbody.get(0), "tr");
			//int num_of_rows = headrowscount+rowsbody.size();
			int headrowscount = 0;
			if (rowshead != null) 
				headrowscount = rowshead.size();
			int num_of_rows = rowsbody.size()+headrowscount;
			int cols = CountColumns(rowsbody,rowshead);
			tables[tableindex].tableInTable = s;

			int num_of_columns = cols;
			tables[tableindex].setNum_of_columns(num_of_columns);
			tables[tableindex].setNum_of_rows(num_of_rows);
			tables[tableindex].CreateCells(num_of_columns, num_of_rows);
			Cell[][] cells = tables[tableindex].getTable_cells();
			tables[tableindex] = ProcessTableHeader(tables[tableindex],cells, rowshead, headrowscount, num_of_columns);
			Statistics.addColumn(num_of_columns);
			Statistics.addRow(num_of_rows);
			tables[tableindex] = ProcessTableBody(tables[tableindex],cells, rowsbody, headrowscount, num_of_columns);
			tables[tableindex].setTable_cells(cells);		
			//Print cells
			for(int j = 0; j<cells.length;j++)
			{
				for(int k = 0; k<cells[j].length;k++)
				{
					System.out.println(j+","+k+": "+cells[j][k].getCell_content());
				}
			}
			System.out.println("Number of rows: "+num_of_rows);
			System.out.println("Number of columns: "+num_of_columns);
			tableindex++;
			}
			if(tb.size()==0 && tableindex<numOfTables)
			{
				Statistics.addTable();
				String label = readTableLabel(tablesxml.item(i));
				
				tables[tableindex] = new Table(label);
				tables[tableindex].setDocumentFileName("PMC"+article.getPmc());
				tables[tableindex].setXml(Utilities.CreateXMLStringFromSubNode(tablesxml.item(i)));
				System.out.println("Table title:"+tables[tableindex].getTable_title());
				String caption = readTableCaption(tablesxml.item(i));
				tables[tableindex].setTable_caption(caption);
				String foot = ReadTableFooter(tablesxml.item(i));
				tables[tableindex].setTable_footer(foot);
				System.out.println("Foot: "+foot);
				Statistics.ImageTable(FileName);
				tables[tableindex].setNoXMLTable(true);
				tableindex++;
				continue;
			}
		}// end for tables
		if(TablInExMain.TypeClassify){
			for(int i = 0;i<numOfTables;i++)
			{
				SimpleTableClassifier.ClassifyTableByType(tables[i]);
				tables[i].printTableStatsToFile("TableStats.txt");
			}
		}
		if(TablInExMain.ComplexClassify){
			for(int i = 0;i<numOfTables;i++)
			{
				SimpleTableClassifier.ClassifyTableByComplexity(tables[i]);
				//tables[i].printTableStatsToFile("TableStats.txt");
			}
		}
		return article;
	}
	
	/**
	 * Gets the children by tag name.
	 *
	 * @param parent the parent
	 * @param name the name
	 * @return the children by tag name
	 */
	public static List<Node> getChildrenByTagName(Node parent, String name) {
	    List<Node> nodeList = new ArrayList<Node>();
	    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
	      if (child.getNodeType() == Node.ELEMENT_NODE && 
	          name.equals(child.getNodeName())) {
	        nodeList.add((Node) child);
	      }
	    }

	    return nodeList;
	  }
	
	
	
	
}