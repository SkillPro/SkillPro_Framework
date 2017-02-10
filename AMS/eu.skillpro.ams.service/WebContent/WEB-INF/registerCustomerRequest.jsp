<!DOCTYPE HTML>
<html>
<head>
	<title>New customer order</title>
	<link type="text/css" rel="stylesheet" href="<c:url value="templates/style.css" />" />	
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>
<body>
	<%@ include file = "header.jsp" %>
	<div class="main">
			<h1>New customer Order</h1>
			<div class="register">
				<form id="register-form" action="registerCustomerRequest" method="post">
						<p>
							<label for="name">Name <span class="required">*</span>	</label>
							<input type="text" name="name" id="name" spellcheck="false" placeholder="" value="<c:out value="${customerRequest.customerName}"/>" size="50" maxlength="60"/>
							<span class="error">${form.errors['name']}</span>
						</p>
						<p>
							<label for="seeID">SEE-ID: </label><select name="seeID" id="seeID"
								size="1">
								<option value="217137e4-d23e-11e4-b49d-88b718c45141">Workplace 1</option>
								<option value="2c8db814-d23e-11e4-92d1-88b718c45141">Workplace 2</option>
								<option value="352354b6-d23e-11e4-aba8-88b718c45141">Workplace 3</option>
							</select>
						</p>
						<p>
							<label for="chocolate">Chocolate: </label><select name="chocolate" id="chocolate"
								size="1">
								<option value="blue">Whole milk chocolate</option>
								<option value="white">Yoghurt chocolate</option>
								<option value="violet">Nugat chocolate</option>
								<option value="yellow">Cornflakes chocolate</option>
								<option value="red">Marzipan chocolate</option>
								<option value="brown">Cookie chocolate</option>
								<option value="green">Hazelnut chocolate</option>
							</select>
						</p>
						<p>
							<label for="color1">Color 1: <span class="required">*</span> </label>
							<select name="color1" id="color1" size="1">
								<option>Black</option>
								<option>White</option>
								<option>Yellow</option>
								<option>Blue</option>
								<option>Red</option>
							</select>
						</p>
						<p>
							<label for="color2">Color 2: <span class="required">*</span> </label>
							<select name="color2" id="color2" size="1">
								<option>Black</option>
								<option>White</option>
								<option>Yellow</option>
								<option>Blue</option>
								<option>Red</option>
							</select>
						</p>
						<p>
							<label for="color3">Color 3: <span class="required">*</span> </label>
							<select name="color3" id="color3" size="1">
								<option>Black</option>
								<option>White</option>
								<option>Yellow</option>
								<option>Blue</option>
								<option>Red</option>
							</select>
						</p>
						<div class="roundedOne">
							<label for="humanWorker">Human Worker: <span class="required">*</span> </label>
							<input type="checkbox" name="humanWorker" id="workInLine" value="1" />
						</div>
						<p class="info">${ form.result }
							<input type="submit" value="Send order" id="send-oder" class="button"/>
							<input type="reset" value="Abbrechen" class="button" /> <br />
						</p>
				</form>
			</div>
    </div>
</body>
</html>