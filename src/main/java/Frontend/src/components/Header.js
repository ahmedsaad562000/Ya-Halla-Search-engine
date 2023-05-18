import React, { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
// import logo from '../logo-w.png'
import logo from '../HALLA.png'





const Header = ({query}) => {
  




   
  return (
    <header>
    <nav class=" border-neutral-200 py-8  bg-slate-800  flex flex-row">
    <a href="/" class="pl-48 mb-4 flex items-center">
    <img class="h-12   " src={logo} alt="logo" />
    </a>

        <div class="flex flex-col flex-wrap justify-between items-left ml-12 max-w-screen-xl">
          

            <input value={query ? query : ""} style={{width: "700px"}}  type="text" id="default-search" class="mt-0   p-4 pl-10 text-sm text-white border rounded-lg   bg-gray-700  border-neutral-600  placeholder-neutral-400   focus:ring-red-500  focus:border-red-500 appearance-none " placeholder="text" required />

      
           
        </div>
        {/* <div className='flex flex-row mt-6'>
        <svg class="ml-6 h-12 mx-auto mb-3 text-neutral-400  text-gray-600" viewBox="0 0 24 27" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M14.017 18L14.017 10.609C14.017 4.905 17.748 1.039 23 0L23.995 2.151C21.563 3.068 20 5.789 20 8H24V18H14.017ZM0 18V10.609C0 4.905 3.748 1.038 9 0L9.996 2.151C7.563 3.068 6 5.789 6 8H9.983L9.983 18L0 18Z" fill="currentColor"/>
          </svg> 
          <p class="ml-4 text-xl font-medium text-neutral-400 ">"Abeelo Smart, Abeelo Efficiently: Feel the Power of our Edeelo"</p>

        </div> */}
    </nav>
</header>
  )
}

export default Header