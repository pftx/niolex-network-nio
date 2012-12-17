<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>配置中心</title>
<link rel="icon" href="./resources/img/icon.ico" type="image/ico">
<!-- Le styles -->
<link href="bootstrap/css/bootstrap.min.css" rel="stylesheet">
<link href="bootstrap/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="resources/css/group.css" rel="stylesheet">
</head>

<body>
	<div class="navbar">
		<div class="navbar-inner">
			<a class="brand" href="#">配置中心</a>
			<ul class="nav">
				<li><a href="index.html">Home</a></li>
				<li><a href="config">配置组</a></li>
				<li class="active"><a href="group">查看配置</a></li>
				<li><a href="modify">修改配置</a></li>
				<li><a href="user">用户中心</a></li>
			</ul>
			<span class="nav user-info"> <i class="icon-hand-right"></i>&nbsp;${userName}
			</span>
			<ul class="nav pull-right">
				<li><a href="#">Link</a></li>
				<li class="divider-vertical"></li>
				<li class="dropdown"><a href="#" class="dropdown-toggle"
					data-toggle="dropdown">系统<b class="caret"></b></a>
					<ul class="dropdown-menu">
						<li><a data-toggle="modal" data-target="#mymodal"
							href="#mymodal">Sign In</a></li>
						<li><a href="login?signout=3">Sign Out</a></li>
						<li class="divider"></li>
						<li><a href="#">About This</a></li>
					</ul>
				</li>
			</ul>
		</div>
		<!-- /navbar-inner -->
	</div>
	<!-- /navbar -->
	<div class="main">
		<!--modal's content-->
		<div id="mymodal" class="modal hide fade">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h3 id="results">登录到用户中心</h3>
			</div>
			<!--Modal header-->
			<div class="modal-body">
				<div class="row">
					<div class="span4">
						<form id="login" action="login" method="post"
							class="modal-form form-horizontal">
							<div class="control-group">
								<label class="control-label">User Name:</label>
								<div class="controls">
									<input type="text" id="userName" name="username" required>
								</div>
							</div>
							<div class="control-group">
								<label class="control-label">Password:</label>
								<div class="controls">
									<input type="password" id="password" name="password" required>
								</div>
							</div>
						</form>
					</div>
					<div class="span1" id="target"></div>
				</div>
			</div>
			<!--Modal body-->
			<div class="modal-footer">
				<input type="submit" class="btn btn-primary" value="Sign In"
					form="login"> <input type="button" class="btn btn-danger"
					data-dismiss="modal" value="Close" form="login">
			</div>
			<!--Modal footer-->
		</div>
		<!--Modal-->
		<div class="main_query">
			<input class="search-query input-large" id="groupName" type="text"
				placeholder="Type Group Name Here" autofocus required> <a
				class="btn btn-info" onClick="handle_query()" type="submit"
				rel="tooltip" title="到配置中心查询该配置组"><i
				class="icon-white icon-search"></i> 查询</a>
		</div>
		<div class="main_body">
			<div class="alert alert-error hide width9">
				<strong id="alertMessage">${alertMessage}</strong>
			</div>
			<table id="dataTable"
				class="table table-hover table-bordered main_table hide">
				<thead>
					<tr>
						<th>配置KEY</th>
						<th>更新时间</th>
						<th>配置内容</th>
					</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
		<!--modal's content-->
		<div id="modalDetail" class="modal hide width8" tabindex="-1">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">&times;</button>
				<h3 id="detailKey"></h3>
			</div>
			<!--Modal header-->
			<div class="modal-body">
				<pre id="detailValue"></pre>
			</div>
			<!--Modal body-->
			<div class="modal-footer">
				<a id="detailModify" class="btn btn-success">Modify</a>
				<button class="btn btn-danger" data-dismiss="modal">Close</button>
			</div>
			<!--Modal footer-->
		</div>
		<!--Modal-->
	</div>
	<!-- /container -->

	<!-- Placed at the end of the document so the pages load faster -->
	<script src="bootstrap/js/jquery.min.js"></script>
	<script src="bootstrap/js/bootstrap.min.js"></script>
	<script src="resources/js/index.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			init_bind();
		});
	</script>
</body>
</html>