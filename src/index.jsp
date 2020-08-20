<%@ include file="includes/header.jsp" %>

<div class="animated bounceInDown" style="font-size:48pt; font-family:arial; color:#990000; font-weight:bold">Web Opinion Visualiser</div>

</p>&nbsp;</p>&nbsp;</p>

<table width="600" cellspacing="0" cellpadding="7" border="0">
	<tr>
		<td valign="top">

			<form bgcolor="white" method="POST" action="doProcess">
				<fieldset>
					<legend><h3>Specify Details</h3></legend>
				
					<b>Number of Words to Visualize</b>
				
					<select name="cmbOptions">
						<option>10</option>
						<option>20</option>
						<option selected>32</option>
					</select>
					<p>
					
					Only use the following JARs with your application. You can assume that they have already been added to the Tomcat CLASSPATH:
					<p>
					<ol>
						<li><a href="https://jsoup.org">JSoup</a>
						<li><a href="http://jfuzzylogic.sourceforge.net/html/index.html">JFuzzyLogic</a>
						<li><a href="https://github.com/jeffheaton/encog-java-core">Encog</a>
					</ol>	
							
			
					<p/>

					<b>Enter Text :</b><br>
					<input name="query" size="100">	
					<p/>

					<center><input type="submit" value="Search & Visualise!"></center>
				</fieldset>							
			</form>	

		</td>
	</tr>
</table>
<%@ include file="includes/footer.jsp" %>

