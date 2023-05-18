import './App.css';
import Header from './components/Header';
import logo from "./logo.png";

function App() {
  return (
    <div className="App">
   
   <section class="bg-gray-50">
  <div class="flex flex-col items-center justify-center px-6 py-8 mx-auto md:h-screen lg:py-0">
  <img class="h-60 mb-12  mr-2" src={logo} alt="logo" />
  <div>
        <input style={{width: "700px"}} type="text" name="query" id="query" class="w-ful bg-gray-50 border-2  border-gray-300 text-gray-900 sm:text-md rounded-2xl focus:ring-none focus:ring-0 focus:ring-offset-0  p-2.5  " placeholder="Enter you query" required={true} />
    </div>
    <button type="submit" class="w-32 mt-2  text-white bg-red-600 hover:bg-red-700 focus:ring-4 focus:outline-none focus:ring-red-300 font-semibold rounded-lg text-sm px-5 py-2.5 text-center ">ABEELO</button>


      {/* <div class="w-full bg-white rounded-lg shadow md:mt-0 sm:max-w-md xl:p-0">
          <div class="p-6 space-y-4 md:space-y-6 sm:p-8">
        
              <form class="space-y-4 md:space-y-6" action="#">
                  <div>
                      <input type="text" name="query" id="query" class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-primary-600 focus:border-primary-600 block w-full p-2.5  " placeholder="name@company.com" required="" />
                  </div>
             
            
                  <button type="submit" class="w-full text-white bg-red-600 hover:bg-red-700 focus:ring-4 focus:outline-none focus:ring-primary-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center ">Sign in</button>
              
              </form>
          </div>
      </div> */}
  </div>
</section>   
    </div>
  );
}

export default App;
