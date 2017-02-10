<!DOCTYPE HTML>
<html>
<head>
	<title>The order has been placed</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link type="text/css" rel="stylesheet" href="<c:url value="templates/style.css" />" />	
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
</head>
<body>
	<%@ include file = "header.jsp" %>
	<div class="main">
	    <h1>${ form.result }</h1>
	    <div class="result_view">
		    <p>Name : <c:out value="${ customerRequest.customerName }"/></p>
		    <p>SEE-ID : <c:out value="${ customerRequest.seeID }"/></p>
		    <p>Order-ID : <c:out value="${ customerRequest.orderID }"/></p>
		    <p>Products : <c:out value="${ customerRequest.getFormattedProducts() }"/></p>
		    <p>Human Worker : <c:out value="${ customerRequest.humanSEE }"/></p>
	    </div>
	    <p><a href="registerCustomerRequest">New order</a></p>
    </div>
    
</body>
</html>