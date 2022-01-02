package matrix;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Matrix {

	public int[][] matrix;
	int[][] trans;
	public int x, y;
	private boolean transposed;

	public Matrix(int x, int y){
		matrix = new int[x][y];
		this.x = x;
		this.y = y;

	}

	/*
	 * This method takes in a 2d matrix array and returns the transposed matrix
	 * https://en.wikipedia.org/wiki/Transpose
	 */
	private int[][] transpose(int[][] arr){
		this.trans = new int [arr[0].length][arr.length];
		for(int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				trans[j][i] = arr[i][j];
			}
		}
		this.transposed = true;
		return trans;
	}

	public void set(int[][] in){
		this.matrix = in;
	}

	// DO NOT MODIFY THIS METHOD
	public void load(String path) throws IOException{
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(path));
		}catch(FileNotFoundException e){
			System.err.println("file not found: " + path);
		}
		int row = 0;
		while(true){
			String line = br.readLine();
			if(line == null){
				break;
			}
			String arr[] = line.split(" ");
			for(int i = 0; i < arr.length; i ++) {
				matrix[row][i] = Integer.parseInt(arr[i]);
			}
			row++;
		}
		trans = transpose(matrix);
		transposed = true;
	}

	// DO NOT MODIFY THIS METHOD
	public String toString(){
		String aString = "";
		for(int row = 0; row < matrix.length; row++) {
			for(int col = 0; col < matrix[row].length; col++) {
				aString += " " + matrix[row][col];
			}
			aString += "\r\n";
		}
		return aString;
	}

	/*
	 * This is a single-threaded matrix multiply.
	 * Takes in a matrix and multiplies itself by it so (this means x*b)
	 */
	public Matrix multiply(Matrix b){
		if (b.matrix.length == 0 || b.matrix.length != this.matrix[0].length) {
			return null;
		}
		Matrix r = new Matrix(b.matrix.length, b.matrix[0].length);
		for (int i = 0; i < this.matrix.length; i++) { // aRow
			for (int j = 0; j < b.matrix[i].length; j++) { // bColumn
				for (int k = 0; k < this.matrix[i].length; k++) { // aColumn
					r.matrix[i][j] += this.matrix[i][k] * b.matrix[k][j];
				}
			}
		}
		return r;
	}

	/*
	 * This method takes in a matrix, and a number of threads and uses that 
	 * number of threads to multiply the two matrices together. It should be in 
	 * the order (this means x*m) 
	 */
	public Matrix multiply(Matrix m, int threads) {
		//like findSum()
		if (m.matrix.length == 0 || m.matrix.length != this.matrix[0].length) {
			return null;
		}
		Matrix result = new Matrix(m.matrix.length, m.matrix[0].length);
		Thread[] multiplyWorker =  new Thread[threads];
		int index = 0;
		if (threads == this.matrix.length) {
			for (int i = 0; i < multiplyWorker.length; i++) {
				multiplyWorker[i] = new Thread(new multiplyWorker(this.matrix,
						m.matrix, result.matrix, index));
				multiplyWorker[i].start();
				index++;
			}

			for (Thread multiplyWorkers : multiplyWorker) {
				try {
					multiplyWorkers.join();
				} catch (InterruptedException e) {
					//RIP
				}
			}

		} else if (threads > this.matrix.length){
			for (int i = 0; i < this.matrix.length; i++) {
				multiplyWorker[i] = new Thread(new multiplyWorker(this.matrix, 
						m.matrix, result.matrix, index));
				multiplyWorker[i].start();
				index++;
			}

			for (int i = 0; i < this.matrix.length; i++) {
				try {
					multiplyWorker[i].join();
				} catch (InterruptedException e) {
					//RIP
				}
			}

		} else {	//threads < m.matrix.length
			int repeat = (int) Math.ceil(this.matrix.length/(threads));
			for (int j = 0; j < threads; j++) {
				for (int i = 0; i < repeat; i++) {
					multiplyWorker[j] = new Thread(new multiplyWorker(this.matrix, 
							m.matrix, result.matrix, index));
					multiplyWorker[j].start();
					index++;
				}

			}
			for (int i = 0; i < threads; i++) {
				try {
					multiplyWorker[i].join();
				} catch (InterruptedException e) {
					//RIP
				}
			}
		}

		return result;
	}

	/*
	 * A method that should take in a matrix and determine if it is equal to 
	 * this matrix
	 */
	@Override
	public boolean equals(Object in) {
		if (!(in instanceof Matrix)) return false;

		try {
			//it is safe to cast because if in is not a Matrix, it cant be equal
			Matrix temp = (Matrix) in;
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[i].length; j++) {
					if (matrix[i][j] != temp.matrix[i][j]) {
						return false;
					}
				}
			}
		} catch(IndexOutOfBoundsException e) {
			return false;	//they are of different dimensions, therefor not equal
		}

		return true;
	}

	
	public static void main(String[] args){
		System.out.println("running");
		int size = 3000;
		Matrix a = new Matrix(size, size);
		Matrix b = new Matrix(size, size);

		int[][] m1 = generateMatrix(size, size);
		int[][] m2 = generateMatrix(size, size);

		a.matrix = m1;
		b.matrix = m2;
		

		Date start4 = new Date();
		Matrix c4 = a.multiply(b, 1);
		Date end4 = new Date();
		
		System.out.println("\nTime taken in seconds (1 threads): " + (end4.getTime() - start4.getTime()) / 1000 + '\n');

		Date start2 = new Date();
		Matrix c2 = a.multiply(b,8);	//threads < m.matrix.length
		Date end2 = new Date();

		System.out.println("\nTime taken in seconds (8 threads): " + (end2.getTime() - start2.getTime()) / 1000);

		
	}
	
	private static int[][] generateMatrix(int rows, int columns) {
		int[][] result = new int[rows][columns];
		Random random = new Random();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				result[i][j] = random.nextInt(100) * 10;
			}
		}
		return result;
	}
}




class multiplyWorker implements Runnable{

	private int[][] matrixA, matrixB, result;
	private int index;

	public multiplyWorker (int[][] matrixA, int[][] matrixB, int[][] result,
			int index) {
		this.matrixA = matrixA;
		this.matrixB = matrixB;
		this.result = result;
		this.index = index;

	}
	@Override
	public void run() {
		for (int i = 0; i < matrixB[0].length; i++) {
			result[index][i] = 0;
			for (int j = 0; j < matrixA[index].length; j++) {
				result[index][i] += matrixA[index][j] * matrixB[j][i];
			}
		}
	}

}
