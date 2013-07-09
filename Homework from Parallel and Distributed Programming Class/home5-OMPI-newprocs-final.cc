//*********************************************************
//* Class:	CS 4000 - Parallel & Distributed Computing
//*
//* Author:	Isaac N. Van Houten
//* Email:	iv104109@ohio.edu
//*
//* Description: Compute maximum weighted independent sets
//*		 for simple undirected graphs.
//*
//* Date: 		March 17th, 2013
//*********************************************************/
#include <iostream>
#include <fstream>
#include <sstream>
#include <vector>
#include <list>
#include <assert.h>
#include <cstdlib>
#include <cmath>
#include <mpi.h>

using namespace std;

int n;			//Size of the adj_list, it's more helpful as a global variable.
vector< list<int> > binaryGraph; //A global vector that will have all the nodes' data.

//Dr. Jeudes' Broadcast_Receive Function
void BroadCast_Receive(vector<int> &array, int k, int n) {
	
  // First, broadcast the size of the array....
  int buffer[2];
  buffer[0] = array.size();
  // Everybody executes the same code!
  MPI_Bcast(buffer,1,MPI_INT,0,MPI_COMM_WORLD);

  // Now, everyone knows the size....
  array.resize(buffer[0]);

  // Broadcast the entire array.
  MPI_Bcast(&array[0],buffer[0],MPI_INT,0,MPI_COMM_WORLD);

}

//A simple function to see if the integer, x, exists in the vector, y.
bool ismember(int x, vector<int> y){
	for(int i=0; i<y.size(); i++){
		if(x == y[i]){
			return(true);
		}
	}
	return(false);
}

//lessdegree2 finds the maximum and returns it through reference addressing.
//if the maximum is larger than 2, then lessdegree2 returns true.
bool lessdegree2(int &v, vector< list<int> > &adj_list, vector<int> &wght_list){

	int size;
	int max=0;

	for(int i=0; i<n; i++){
		size = 0;
		list<int>::iterator it;
		if(wght_list[i] != -1){
			for(it=adj_list[i].begin(); it != adj_list[i].end(); ++it){
				if(wght_list[*it] != -1){
					size+=1;
				}
			}
		}
		if(size > max){
			max = size;
			v = i;
		}
	}
	if(max >= 2){
		return(false);
	}
	else{
		return(true);
	}
}

//A function that checks to see how many neighbors a vertex has
//This is necessary because instead of deleting vertices, I just
//set their weights to -1.
int neighbors(list<int> alist, vector<int> wght_list){

	list<int>::iterator it;
	int counter = 0;
	for(it = alist.begin(); it != alist.end(); ++it){
		if(wght_list[*it] != -1){
			counter += 1;
		}
	}
	return(counter);
}

//This function will execute when maxWset function has cut down the graph
//to simply connected (less than 2 connections) vertices.
vector<int> basecase(vector< list<int> > &adj_list, vector<int> wght_list){

	vector<int> throwaway;
	vector<int> w_set;
	w_set.resize(1);
	w_set[0] = 0;

	for(int i=0; i<n; i++){

		if(!ismember(i, throwaway)){
			if(wght_list[i] != -1){
				if(neighbors(adj_list[i], wght_list) == 0){
					w_set[0] += wght_list[i];
					w_set.push_back(i);
				}
				else {
					int x, y, neighbor;
					x = wght_list[i];
					list<int>::iterator it;
					for(it = adj_list[i].begin(); it != adj_list[i].end(); ++it){
						if(wght_list[*it] != -1){
							neighbor = *it;
							y = wght_list[*it];
							throwaway.push_back(neighbor);
						}
					}
					if(x >= y){
						w_set[0] += x;
						w_set.push_back(i);
					} else {
						w_set[0] += y;
						w_set.push_back(neighbor);
					}
				}
			}
			throwaway.push_back(i);
		}
	}
	return(w_set);
}

//The function returns true if the vector is at least degree 2
bool atleastdegree2(int x, vector< list<int> > &adj_list){
	int count=0;
	for(int i=0; i<n; i++){
			list<int>::iterator it;
			for(it = adj_list[i].begin(); it != adj_list[i].end(); ++it){
				if(*it == x){
					count += 1;
					if(count >= 2){
						return(true);
					}
				}
			}
	}
	return(false);
}

//Return a weight-list with v's weight changed so that
//it will be ignored.
vector<int> minus_v(int v, vector<int> wght_list){
	wght_list[v]=-1;
	return(wght_list);
}

//Change weight of all of v's neighbors. (and v)
vector<int> minus_v_nghd(int v, vector< list<int> > &adj_list, 
						 vector<int> &wght_list){

	vector<int> new_wght_list(wght_list);

	list<int>::iterator it;
	for(it = adj_list[v].begin(); it != adj_list[v].end(); ++it){
		new_wght_list[*it]=-1;
	}
	return(new_wght_list);
}

void maxSet(int parent, int current, vector< list<int> > &adj_list, vector<int> wght_list){

	if(current > 0){

		list<int> templist;

		templist.push_back(parent);		//Parent Node
		templist.push_back(current);	//Current Node
		
		int vstar=-1;		//Our vertex that will have at least two neighbors
		if(lessdegree2(vstar, adj_list, wght_list)){

			templist.push_back(0);		//Child 1
			templist.push_back(0);		//Child 2

			for(int i = 0; i < wght_list.size(); i++){
				templist.push_back(wght_list[i]);	
			}

			binaryGraph.push_back(templist);

		} 
		else {

			int firstchild = current*2;
			int secondchild = current*2 + 1;

			maxSet(current, firstchild, adj_list, minus_v(vstar, wght_list));
			maxSet(current, secondchild, adj_list, minus_v_nghd(vstar, adj_list, wght_list));

			templist.push_back(firstchild);
			templist.push_back(secondchild);
		}
	}
	return;
}

//The primary function; maxWset finds the maximum weighted independent set
//by recursively calling itself on the (graph - v*) and (graph - v* - neighborhood)
//and returning the max value of the two.
vector<int> maxWset(vector< list<int> > &adj_list, vector<int> wght_list){

	int vstar=-1;		//Our vertex that will have at least two neighbors
	if(lessdegree2(vstar, adj_list, wght_list)){
		return(basecase(adj_list,wght_list));
	} 
	else {

		vector<int> new_wght_list1;
		vector<int> new_wght_list2;
		new_wght_list1 = minus_v(vstar, wght_list);
		new_wght_list2 = minus_v_nghd(vstar, adj_list, wght_list);

		vector<int> max1(maxWset(adj_list, new_wght_list1));
		vector<int> max2(maxWset(adj_list, new_wght_list2));

		if(max1[0] > max2[0]){
			return(max1);
		}
		else{
			return(max2);
		}
	}
}

//The main function, where the processes are divided up.
int main(int argc, char * argv[]) {

	vector< list<int> > adj_list;	//Vector of adjacency lists
	vector<int> wght_list;			//Vector of weights

	ifstream f1;
	ifstream f2;
	f1.open(argv[1]);
	f2.open(argv[2]);

	int rank, size;				//Rank and size; used for MPI threads.

	//File verification.
	if(f1.fail() || f2.fail()){
		cout << "File opening failed!" <<endl;
		return 1;
	}

	f1 >> n;				//Send first element (size of list), to n.
	adj_list.resize(n);		//Since adjacency list and.
	string line1;			//Temporary string.
	getline(f1, line1);		//ignore last line.

	//Input loop; retrieves values from .dat file
	//and stores them in adj_list
	for(int i=0; i < n; i++){
		
		getline(f1, line1);
		istringstream in1(line1);
		int j;
		in1 >> j;
		assert(j==i);

		while(!in1.eof()){
			int k;
			in1 >> k;
			if(!in1.fail()){
				adj_list[j].push_back(k);
			}
		}
	}

	int f2firstelement;
	f2 >> f2firstelement;	//Get rid of first element.

	while(!f2.eof()){
		int m;
		f2 >> m;
		if(!f2.fail())
			wght_list.push_back(m);
	}

	/*
	//cout << "basecases" <<endl;
	vector<int> basecases;
	for(int i = 0; i < binaryGraph.size(); i++){
		if(binaryGraph[i].front() != -1){
			list<int>::iterator it, it2;
			it = binaryGraph[i].begin();
			++++++it;
			it2 = it;
			++it2;

			if(*it == 0 || *it2 == 0){
				basecases.push_back(i);
			}
		}
	}
	*/

	MPI_Init(NULL, NULL);
	MPI_Comm_size(MPI_COMM_WORLD,&size);
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);
	////cout << "Hello from process " << rank << " of " << size <<endl;

	vector<int> weights;

	//vector< vector<int> > allvectormax;
    //int basecasesize = basecases.size();
	//int zerosbasecasemax[basecasesize];

	if(rank == 0){
		if(size == 1){
			weights = maxWset(adj_list, wght_list);
		} else {

			int vstar=-1;		//Our vertex that will have at least two neighbors
			if(lessdegree2(vstar, adj_list, wght_list)){
				weights = basecase(adj_list,wght_list);
			} 
			else {
				
				vector<int> max1;

				vector<int> temp_wght_list(minus_v(vstar, wght_list));
				int tempbuffer[1];
				tempbuffer[0] = temp_wght_list.size();
				int tempnwl[temp_wght_list.size()];
				for(int i = 0; i < temp_wght_list.size(); i++){
					tempnwl[i] = temp_wght_list[i];
				}
				MPI_Send(tempbuffer, 1, MPI_INT, 1, 0, MPI_COMM_WORLD);
				MPI_Send(tempnwl, temp_wght_list.size(), MPI_INT, 1, 0, MPI_COMM_WORLD);

				if(size > 2){
					vector<int> new_wght_list(minus_v_nghd(vstar, adj_list, wght_list));
					int buffer[1];
					buffer[0] = new_wght_list.size();
					int nwl[new_wght_list.size()];
					for(int i = 0; i < new_wght_list.size(); i++){
						nwl[i] = new_wght_list[i];
					}
					MPI_Send(buffer, 1, MPI_INT, 2, 0, MPI_COMM_WORLD);
					MPI_Send(nwl, new_wght_list.size(), MPI_INT, 2, 0, MPI_COMM_WORLD);
				}
				else{
					max1 = maxWset(adj_list, temp_wght_list);
				}

				//cout << "Process 0, new Weight list sent!"<<endl;

				int buffer2[2];
				MPI_Recv(buffer2, 1, MPI_INT, 1, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
				int max2array[buffer2[0]];
				MPI_Recv(max2array, buffer2[0], MPI_INT, 1, 1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

				int buffer3[2];
				MPI_Recv(buffer3, 1, MPI_INT, 2, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
				int max1array[buffer3[0]];
				MPI_Recv(max1array, buffer3[0], MPI_INT, 2, 2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

				//cout << "Final info received!" <<endl;

				if(size > 2){
					if(max1array[0] > max2array[0]){
						for(int i = 0; i < buffer3[0]; i++){
							weights.push_back(max1array[i]);
						}
					} else {
						for(int i = 0; i < buffer2[0]; i++){
							weights.push_back(max2array[i]);
						}
					}
				}
				else{
					if(max1[0] > max2array[0]){
						weights = max1;
					} else {
						for(int i = 0; i < buffer2[0]; i++){
							weights.push_back(max2array[i]);
						}
					}
				}
			}
		}
	}
	else if(rank != 0) {

		cout << rank << " Executing Process!"<<endl;

		int vstar=-1;		//Our vertex that will have at least two neighbors
		if(lessdegree2(vstar, adj_list, wght_list)){
			weights = basecase(adj_list,wght_list);
		} 
		else {

			int buffer[1];
			if(rank == 1){
				MPI_Recv(buffer, 1, MPI_INT, rank-1, rank-1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			}
			else{
				MPI_Recv(buffer, 1, MPI_INT, rank-2, rank-2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			}

			int nwl[buffer[0]];

			if(rank == 1){
				MPI_Recv(nwl, buffer[0], MPI_INT, rank-1, rank-1, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			}
			else{
				MPI_Recv(nwl, buffer[0], MPI_INT, rank-2, rank-2, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
			}

			//cout << rank << " New Weight List Received!" <<endl;
			//for(int i = 0; i < buffer[0]; i++){
			//	//cout << nwl[i] <<endl;
			//}

			vector<int> new_wght_list;
			for(int i = 0; i < buffer[0]; i++){
				new_wght_list.push_back(nwl[i]);
			}
			
			//Each process takes the left branch, while giving the right branch to
			//a process of +2 more than current rank (i.e. proc 1 gives to proc 3)
			vector<int> temp_wght_list(minus_v(vstar, new_wght_list));
			vector<int> temp_wght_list2(minus_v_nghd(vstar, adj_list, new_wght_list));

			if(rank+2 < size){
				int buffer[1];
				buffer[0] = temp_wght_list2.size();
				int nwl[temp_wght_list2.size()];

				for(int i = 0; i < temp_wght_list2.size(); i++){
					nwl[i] = temp_wght_list2[i];
				}

				MPI_Send(buffer, 1, MPI_INT, rank+2, rank, MPI_COMM_WORLD);
				MPI_Send(nwl, temp_wght_list2.size(), MPI_INT, rank+2, rank, MPI_COMM_WORLD);
		

				vector<int> max;
				max = maxWset(adj_list, temp_wght_list);

				//cout << rank << " Max calculated!" <<endl;
				int buffer2[2];
				buffer2[0] = max.size();

				if(rank == 1){
					MPI_Send(buffer2, 1, MPI_INT, rank-1, rank, MPI_COMM_WORLD);

					int maxarray[max.size()];
					for(int i = 0; i < max.size(); i++){
						maxarray[i] = max[i];
					}

					MPI_Send(maxarray, max.size(), MPI_INT, rank-1, rank, MPI_COMM_WORLD);
				}
				else{
					MPI_Send(buffer2, 1, MPI_INT, rank-2, rank, MPI_COMM_WORLD);

					int maxarray[max.size()];
					for(int i = 0; i < max.size(); i++){
						maxarray[i] = max[i];
					}

					MPI_Send(maxarray, max.size(), MPI_INT, rank-2, rank, MPI_COMM_WORLD);
				}

				//cout << endl;
				//cout << rank << " New max sent!" <<endl;

			}
			else{
				vector<int> max;
				max = maxWset(adj_list, new_wght_list);

				int buffer2[2];
				buffer2[0] = max.size();

				if(rank == 1){
					MPI_Send(buffer2, 1, MPI_INT, rank-1, rank, MPI_COMM_WORLD);

					int maxarray[max.size()];
					for(int i = 0; i < max.size(); i++){
						maxarray[i] = max[i];
					}

					MPI_Send(maxarray, max.size(), MPI_INT, rank-1, rank, MPI_COMM_WORLD);
				}
				else{
					MPI_Send(buffer2, 1, MPI_INT, rank-2, rank, MPI_COMM_WORLD);

					int maxarray[max.size()];
					for(int i = 0; i < max.size(); i++){
						maxarray[i] = max[i];
					}

					MPI_Send(maxarray, max.size(), MPI_INT, rank-2, rank, MPI_COMM_WORLD);
				}
			}
		}
	}

	//cout << rank << " end of finalize" <<endl;
	//cout << rank << " Done with Finalize" <<endl;

	MPI_Finalize();

	if(rank == 0){

		//cout << "Weights = " <<endl;
		for(int i=0; i<weights.size(); i++){
			cout << weights[i] << endl;
		}
	}

	return(0);
}

