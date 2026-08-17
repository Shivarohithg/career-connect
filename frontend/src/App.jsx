import { BrowserRouter, Routes, Route } from "react-router-dom";
import JobDetails from "./pages/JobDetails/JobDetails";

import Navbar from "./components/Navbar/Navbar";
import Jobs from "./pages/Jobs/Jobs";

import Home from "./pages/Home/Home";
import Login from "./pages/Login/Login";
import Register from "./pages/Register/Register";

function App() {

    return (

        <BrowserRouter>

            <Navbar />

            <Routes>

                <Route path="/" element={<Home />} />

                <Route path="/login" element={<Login />} />

                <Route path="/register" element={<Register />} />

                <Route path="/jobs" element={<Jobs />} />

                <Route path="/jobs/:id" element={<JobDetails />} />

            </Routes>

        </BrowserRouter>

    );

}

export default App;
