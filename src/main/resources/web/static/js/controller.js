function submitForm() {
  event.preventDefault();

  var responseDiv = document.getElementById("response").hidden = false;

  // Validate url field is set
  let url = document.forms["createShortUrlForm"]["url"].value;
  let expiryCode = document.forms["createShortUrlForm"]["expiryCode"].value;
  let validationMessage = document.getElementById("urlValidationMessage");
  if (url === null || url == "") {
    document.getElementById("urlValidationMessage").innerHTML = "Must specify a URL";
    responseDiv.hidden = true;
    return false;
  } else {
    document.getElementById("urlValidationMessage").innerHTML = "";
  }

  // Call the server to create the URL resource
  var request = new XMLHttpRequest();

  // Create a POST request to the url resource
  request.open('POST', 'http://localhost:8080/shorty/v1/url', true);
  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');

  request.onload = function () {
    // Process the response and display the results
    var responseData = JSON.parse(this.response);
    document.getElementById("results").hidden = false;
    responseDiv.hidden = false;
    var link = document.getElementById("shortUrlLink")
    link.href = responseData.shortUrl;
    link.innerHTML = responseData.shortUrl;
    var expiryDate = new Date(responseData.expiry);
    document.getElementById("expiryMessage").innerHTML = "(Expires: " + expiryDate.toLocaleString() + " )";
  }

  var urlBody = {
    url: url,
    expiryCode: expiryCode
  }

  // Send request
 request.send(JSON.stringify(urlBody));

}