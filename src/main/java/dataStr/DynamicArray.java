package dataStr;




public class DynamicArray {
    private int[] data;
    private int size = 0;

    private long steps = 0;
    private long moves = 0;
    private long comparisons = 0;

    public DynamicArray(int size){

        this.data = new int[size];
        this.size = 10;

    }
public void add(int x){
        if(data.length == this.size){
            Grow();

        }
        data[this.size] = x;
        size++;
}
private void Grow(){

        int[] newData = new int[data.length*2];
        for (int i= 0; i < size;i++){
            newData[i] = data[i];
            steps++;
            moves++;
        }
        data = newData;
}
public int get(int index){
            if (index >= size || index < 0){
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
            }
        steps++;
        return(data[index]);
}

public boolean contains(int x){
        for(int i =0;i < size;i++){
            comparisons++;
            steps++;
            if(data[i]==x){
                return true;
            }

        }
        return false;
}

 public int  remove(int index){
         if (index >= size || index < 0){
             throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
         }
        int elem = data[index];
        for (int i = index; i <size - 1 ; i++){
            data[i]= data[i+1];
            steps++;
            moves++;

        }
        size--;
        return elem;
 }

  public void add(int index, int x){
      int elem = data[index];
      if (index > size || index < 0){
          throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
      }
      if(size == data.length){
          Grow();
      }


      for(int i = size ;i > index ;i--){
          data[i]= data[i -1];
          steps++;
          moves++;
      }
      data[index]=x;
      size++;

  }






}
