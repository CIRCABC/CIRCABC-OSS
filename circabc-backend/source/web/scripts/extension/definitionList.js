		var ddTree = new Object();
		var countSubDL=0;
		
		ddTree.buildDefinitionList=function(dlId){
			document.getElementById(dlId).className="dlOpen";
			var dlItems = document.getElementById(dlId).childNodes;
			
			for(var i=0; i<dlItems.length;i++){
				if(dlItems[i].tagName == "DT"){
					dlItems[i].rel="closed";
					dlItems[i].className="dtClosed";
					dlItems[i].id=dlId+"dt_"+i;
					dlItems[i].onclick=function(e){
						if(this.rel=="closed"){
							ddTree.showDD(parseInt(this.id.substring(this.id.indexOf('_')+1)), dlId);
						}
						else{
							ddTree.hideDD(parseInt(this.id.substring(this.id.indexOf('_')+1)), dlId);
						}
					}
				}
				else if(dlItems[i].tagName == "DD"){
					dlItems[i].rel="closed";
					dlItems[i].className="ddClosed";

					var otherDL = dlItems[i].childNodes;
					for(var j=0;j<otherDL.length;j++){
						if(otherDL[j].tagName == "DL"){
							countSubDL = countSubDL +1;
							otherDL[j].id="SubDL"+countSubDL;
							ddTree.buildDefinitionList("SubDL"+countSubDL);
						}
					}
				}
			}
			
			ddTree.showDD=function(istart, dlId){
				var dlItems = document.getElementById(dlId).childNodes;
				dlItems[istart].rel="open";
				dlItems[istart].className="dtOpen";
				
				for(var i=istart+1; i<dlItems.length;i++){
					if(dlItems[i].tagName == "DD"){
						dlItems[i].rel="open";
						dlItems[i].className="ddOpen";
					}
					else if(dlItems[i].tagName == "DT"){
						break;
					}
				}
			}
			
			ddTree.hideDD=function(istart, dlId){
				var dlItems = document.getElementById(dlId).childNodes;
				dlItems[istart].rel="closed";
				dlItems[istart].className="dtClosed";
				
				for(var i=istart+1; i<dlItems.length;i++){
					if(dlItems[i].tagName == "DD"){
						dlItems[i].rel="Closed";
						dlItems[i].className="ddClosed";
					}
					else if(dlItems[i].tagName == "DT"){
						break;
					}
				}
			}
			
			ddTree.showAllDD=function(dlId){
				var dlItems = document.getElementById(dlId).childNodes;
				
				for(var i=0; i<dlItems.length;i++){
					if(dlItems[i].tagName == "DD"){
						dlItems[i].rel="open";
						dlItems[i].className="ddOpen";
					}
					else if(dlItems[i].tagName == "DT"){
						dlItems[i].rel="open";
						dlItems[i].className="dtOpen";
					}
				}
			}
			
			ddTree.hideAllDD=function(dlId){
				var dlItems = document.getElementById(dlId).childNodes;
				
				for(var i=0; i<dlItems.length;i++){
					if(dlItems[i].tagName == "DD"){
						dlItems[i].rel="Closed";
						dlItems[i].className="ddClosed";
					}
					else if(dlItems[i].tagName == "DT"){
						dlItems[i].rel="open";
						dlItems[i].className="dtClosed";
					}
				}
			}
			
			ddTree.expandAll=function(dlid, action){
				var dltags=document.getElementById(dlid).getElementsByTagName("dl");
				
				(action=="expand")? ddTree.showAllDD(dlid) : ddTree.hideAllDD(dlid);

				for (var i=0; i<dltags.length; i++){
					dltags[i].className=(action=="expand")? "dlOpen" : "dlClosed";
					var relvalue=(action=="expand")? "open" : "closed";
					dltags[i].setAttribute("rel", relvalue);
					(action=="expand")? ddTree.showAllDD(dltags[i].id) : ddTree.hideAllDD(dltags[i].id);
				}
			}
		}